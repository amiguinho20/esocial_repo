/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.esocial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.protocol.Protocol;

import br.com.esocial.Util.SocketFactoryDinamico;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub;
import jvCert.OnCert;

/**
 *
 * @author Usuario
 */
public class EnvioXml {
	private static final int SSL_PORT = 443;

	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * URL do Web Service de envio de lotes: •
		 * https://webservices.producaorestrita.esocial.gov.br/servicos/
		 * empregador/enviarloteeventos/WsEnviarLoteEventos.svc URL do Web
		 * Service de consulta de resultado de processamento de lotes: •
		 * https://webservices.producaorestrita.esocial.gov.br/servicos/
		 * empregador/consultarloteeventos/WsConsultarLoteEventos.svc
		 */
		try {
			URL url = new URL(
					"https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc");
			/*
			 * 
			 */
			// String[] strCertAlias = null;
			// strCertAlias = OnCert.funcListaCertificados(false);
			OnCert.TAssinaXML tpAssinaXML = new OnCert.TAssinaXML();
			// tpAssinaXML.strAliasTokenCert = strCertAlias[0];
			// KeyStore ks = OnCert.funcKeyStore(strCertAlias[0]);
			/*
			 *             
			 */
			String caminhoDoCertificadoDoCliente = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";
			tpAssinaXML.strAliasTokenCert = caminhoDoCertificadoDoCliente;

			String senhaDoCertificado = "mpt";
			tpAssinaXML.strSenhaCertificado = senhaDoCertificado;
			String arquivoCacerts = "/Users/Amiguinho/Development/tmp/esocial/Cacert-22-04-2018";

			InputStream entrada = new FileInputStream(caminhoDoCertificadoDoCliente);
			KeyStore ks = KeyStore.getInstance("pkcs12");
			try {
				ks.load(entrada, senhaDoCertificado.toCharArray());
			} catch (IOException e) {
				throw new Exception("Senha do Certificado Digital esta incorreta ou Certificado inválido.");
			}

			String alias = "";
			Enumeration<String> aliasesEnum = ks.aliases();
			while (aliasesEnum.hasMoreElements()) {
				alias = (String) aliasesEnum.nextElement();
				System.out.println("alias: " + alias);
				if (ks.isKeyEntry(alias)) {
					break;
				}
			}
			X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);
			PrivateKey privateKey = (PrivateKey) ks.getKey(alias, senhaDoCertificado.toCharArray());
			SocketFactoryDinamico socketFactoryDinamico = new SocketFactoryDinamico(certificate, privateKey);
			socketFactoryDinamico.setFileCacerts(arquivoCacerts);

			Protocol protocol = new Protocol("https", socketFactoryDinamico, SSL_PORT);
			Protocol.registerProtocol("https", protocol);

			// criar a mensagem SOAP

			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			sb.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			sb.append(" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
			sb.append(" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">");
			sb.append(" <soap:Header/>");
			sb.append(" <soap:Body>");
			//
			// Assinar o XML do evento. Informar caminho e arquivo de entrada e
			// saída
			//
			tpAssinaXML.strArquivoXML = "/Users/Amiguinho/Development/tmp/esocial/S1000.xml";
			tpAssinaXML.strArquivoSaveXML = "/Users/Amiguinho/Development/tmp/esocial/S1000_assinado.xml";
			OnCert.funcAssinaXML(tpAssinaXML);

			System.out.println("nrInscEmpregador["+tpAssinaXML.nrInscEmpregador+"]");
			System.out.println("evento["+tpAssinaXML.idEvento+"]");
			
			sb.append(" <eSocial xmlns=\"http://www.esocial.gov.br/schema/lote/eventos/envio/v1_1_0\">");
			sb.append(" <envioLoteEventos grupo=\"1\">");
			sb.append(" <ideEmpregador>");
			sb.append(" <tpInsc>1</tpInsc>");
			sb.append(" <nrInsc>" + tpAssinaXML.nrInscEmpregador + "</nrInsc>");
			sb.append(" </ideEmpregador>");
			sb.append(" <ideTransmissor>");
			sb.append(" <tpInsc>1</tpInsc>");
			sb.append(" <nrInsc>12345</nrInsc>");
			sb.append(" </ideTransmissor>");
			sb.append(" <eventos>");
			sb.append(" <evento " + tpAssinaXML.idEvento + ">"); // este Id é o
																	// mesmo do
																	// Id do
																	// evento
																	// que foi
																	// assinado
																	// e será
																	// envelopado
			//
			// envelopar o XML assinado
			//
			sb.append(tpAssinaXML.xmlAssinado.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
					""));
			sb.append(" </evento>");
			sb.append(" </eventos>");
			sb.append(" </envioLoteEventos>");
			sb.append(" </eSocial>");

			sb.append(" </soap:Body>");
			sb.append(" </soap:Envelope>");
			System.out.println("XML SOAP a ser enviado : " + sb.toString());

			OMElement ome = AXIOMUtil.stringToOM(sb.toString());
			ServicoEnviarLoteEventosStub.LoteEventos_type0 dadosMsgType0 = new ServicoEnviarLoteEventosStub.LoteEventos_type0();
			dadosMsgType0.setExtraElement(ome);

			ServicoEnviarLoteEventosStub.EnviarLoteEventos distEnvioEsocial = new ServicoEnviarLoteEventosStub.EnviarLoteEventos();
			distEnvioEsocial.setLoteEventos(dadosMsgType0);

			ServicoEnviarLoteEventosStub stub = new ServicoEnviarLoteEventosStub(url.toString());
			ServicoEnviarLoteEventosStub.EnviarLoteEventosResponse result = stub.enviarLoteEventos(distEnvioEsocial); // neste
																														// momento
																														// é
																														// solicitada
																														// a
																														// senha
																														// do
																														// token
			result.getEnviarLoteEventosResult().getExtraElement().toString();

			System.out.println(result.getEnviarLoteEventosResult().getExtraElement().toString());
		} catch (Exception ex) {
			Logger.getLogger(EnvioXml.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	/**
	 * Le um arquivo e coloca seu conteudo em um string.
	 * Obs: Usa StandardCharsets.UTF_8, pode ser trocado por Charset.defaultCharset()
	 * @param caminhoArquivo
	 * @return 
	 * @throws IOException
	 */
	public static String arquivoParaString(String caminhoArquivo) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(caminhoArquivo));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
