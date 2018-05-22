package br.com.esocial;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.certificado.TipoCertificadoA3;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub;

/**
 * 
 * @author eduardo.csilva
 *
 * Regras para assinatura: validacoes de CPF e CNPJ do certificado: http://portal.esocial.gov.br/manuais/orientacoes-assinatura-digital-e-procuracao-eletronica
 */
public class EnvioXML3 {

	public static void main(String args[]) throws Exception {
		System.out.println("java version: " + System.getProperty("java.version"));

//		String caminhoXML = "/Users/Amiguinho/Development/tmp/esocial/S1000.xml";
//		String caminhoCacerts = "/Users/Amiguinho/Development/tmp/esocial/certificados/Cacert-22-04-2018_v3";
//		String caminhoCertificadoA1 = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";

		String caminhoXML = "C:/desenv/sandbox/esocial/S1000CPF.xml";
		String caminhoCacerts = "C:/desenv/certificados/cacert/Cacert-22-04-2018";
		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = args[0];		
		String senhaCertificadoA3 = args[1];
		
		//Certificado certificado = EsocialUtils.getCertificadoA1(caminhoCertificadoA1, senhaCertificadoA1);
		Certificado certificado = EsocialUtils.getCertificadoA3(senhaCertificadoA3);
		EsocialUtils.inicializarCertificadSocketDinamico(caminhoCacerts, certificado);
		
		String xml = arquivoParaString(caminhoXML);
		
		String xmlAssinado = EsocialUtils.assinarXML(xml, certificado);
		
		String xmlEventos = enveloparDummyEventos(xmlAssinado);
		String xmlEnvelopado = EsocialUtils.enveloparSoap(xmlEventos);
		//xmlEnvelopado = format(xmlEnvelopado, false);

		URL url = new URL(
				"https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc");

		System.out.println(EsocialUtils.formatarXml(xmlEnvelopado, false));

		String xmlRetorno = EsocialUtils.chamarWs(xmlEnvelopado, url.toString());
		
		System.out.println(
				"\n\nRETORNO DO ESOCIAL \n");
		System.out.println(EsocialUtils.formatarXml(xmlRetorno, false));


	}



	/**
	 * StandardCharsets.UTF_8, pode ser trocado por Charset.defaultCharset()
	 */
	public static String arquivoParaString(String caminhoArquivo) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(caminhoArquivo));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	
	/**
	 * Texto fixo \"ID\" 2 posições  
	 * Tipo de Inscrição do Empregador: 1 posição 1: CNPJ 2: CPF 
	 * Número de inscrição do Empregador: 14 posições (completar com zeros à direita). 
	 * Ano/mês/Dia de geração do evento (AAAAMMDD) 8 posições Hora/Minuto/Segundo de geração do evento 6 posições Sequencial da chave.
	 * Incrementar somente quando ocorrer geração de eventos na mesma data e segundo 5 posições - " 
	 * 
     * ID9888888888888887777777766666655555
	 * __-______________--------______-----
	 * | |       |          |      |    |
	 * | |       |          |      |    +---> 5 posicoes, sequencial
	 * | |       |          |      +--------> 6 posicoes HHMMSS (hora-minuto-segundo)
	 * | |       |          +---------------> 8 posicoes AAAAMMDD (ano-mes-dia)
	 * | |       +--------------------------> 14 posicoes CPF/CNPJ
	 * | +----------------------------------> 1 posicao: 1-CPF ou 2-CNPJ
	 * +------------------------------------> 2 posicoes fixas "ID"
	 * 
	 * fonte: https://portal.esocial.gov.br/manuais/mensagenssistemaesocialv1-3.pdf
	 */
	public static String enveloparDummyEventos(String xml) {
		StringBuilder sb = new StringBuilder();
		sb.append("<eSocial xmlns=\"http://www.esocial.gov.br/schema/lote/eventos/envio/v1_1_1\">");
		sb.append("<envioLoteEventos grupo=\"1\">");
		sb.append("     <ideEmpregador>");
		sb.append("         <tpInsc>2</tpInsc>");
		sb.append("         <nrInsc>10071871829</nrInsc>");
		sb.append("     </ideEmpregador>");
		sb.append("     <ideTransmissor>");
		sb.append("         <tpInsc>2</tpInsc>");
		sb.append("         <nrInsc>10071871829</nrInsc>");
		//sb.append("         <nrInsc>12345678901</nrInsc>");
		sb.append("     </ideTransmissor>");
		sb.append("     <eventos>");
		//sb.append("         <evento Id=\"ID1123456780000002018050716254600001\">");
		sb.append("         <evento Id=\"ID2100718718290002018050716254600001\">");
		sb.append(xml);
		sb.append("         </evento>");
		sb.append("     </eventos>");
		sb.append("</envioLoteEventos>");
		sb.append("</eSocial>");

		return sb.toString();

	}

	

}
