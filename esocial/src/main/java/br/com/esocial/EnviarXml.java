package br.com.esocial;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.exception.CertificadoException;

/**
 * 
 * @author eduardo.csilva
 * Dependencia: 
 *  1) um arquivo "cacaerts" customizado com os enderecos de dominios dos webservices \n
 *     @see http://www.javac.com.br/jc/posts/list/2736-arquivo...reinf-atualizado-22042018.page
 *     @see https://github.com/Samuel-Oliveira/Java_Certificado/blob/master/src/main/java/br/com/samuelweb/certificado/util/CacertUtil.java 
 *  2) um certificado digital (A1 ou A3) do tipo e-CPF ou e-CNPJ, com raiz do ICP Brasil, para assinatura de eventos e comunicação segura com o webservice.
 *  3) eventualmente, podera ser necessario a instalacao dos certificados 3 ultimos certificados do ICP Brasil: https://certificados.serpro.gov.br/serproacf/certificate-chain
 *
 * Regras para assinatura: validacoes de CPF e CNPJ do certificado: http://portal.esocial.gov.br/manuais/orientacoes-assinatura-digital-e-procuracao-eletronica
 */
public class EnviarXml {
	
	private static final String END_POINT = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc";
	
	private Certificado certificado;

	/**
	 * Uso com o certificado A1
	 * @param caminhoCacerts
	 * @param caminhoCertificadoA1
	 * @param senhaCertificadoA1
	 */
	public EnviarXml(String caminhoCacerts, String caminhoCertificadoA1, String senhaCertificadoA1) {
		try {
			certificado = EsocialUtils.getCertificadoA1(caminhoCertificadoA1, senhaCertificadoA1);
			EsocialUtils.inicializarCertificadSocketDinamico(caminhoCacerts, certificado);
		} catch (CertificadoException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Uso com o certificado A3 (O CERTIFICADO PRECISA ESTAR CONECTADO NA USB)
	 * @param caminhoCacerts
	 * @param senhaCertificadoA3
	 */
	public EnviarXml(String caminhoCacerts, String senhaCertificadoA3) {
		try {
			certificado = EsocialUtils.getCertificadoA3(senhaCertificadoA3);
			EsocialUtils.inicializarCertificadSocketDinamico(caminhoCacerts, certificado);
		} catch (CertificadoException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String args[]){
		System.out.println("java version: " + System.getProperty("java.version"));

//		String caminhoXML = "/Users/Amiguinho/Development/tmp/esocial/S1000.xml";
//		String caminhoCacerts = "/Users/Amiguinho/Development/tmp/esocial/certificados/Cacert-22-04-2018_v3";
//		String caminhoCertificadoA1 = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";

		String caminhoXML = "C:/desenv/sandbox/esocial/S1000CPF.xml";
		String caminhoCacerts = "C:/desenv/certificados/cacert/Cacert-22-04-2018";
		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = args[0];		
		String senhaCertificadoA3 = args[1];
		
		EnviarXml enviarXml = new EnviarXml(caminhoCacerts, caminhoCertificadoA1, senhaCertificadoA1);
				
		String xml = arquivoParaString(caminhoXML);
		String xmlRetorno = enviarXml.enviar(xml);
		
		System.out.println(EsocialUtils.formatarXml(xmlRetorno, false));
		
	}
	
	
	public String enviar(String xmlEvento) {		
		String xmlAssinado = EsocialUtils.assinarXml(xmlEvento, certificado);
		String id = EsocialUtils.xpathFinder(xmlAssinado, "/*[local-name()='eSocial']/*[local-name()='evtInfoEmpregador']/@Id");
		String xmlEventos = enveloparDummyEventos(xmlAssinado, id);
		String xmlEnvelopado = EsocialUtils.enveloparSoap(xmlEventos);
		String xmlRetorno = EsocialUtils.chamarEnviarWs(xmlEnvelopado, END_POINT);
		return xmlRetorno;
		
	}


	/**
	 * StandardCharsets.UTF_8, pode ser trocado por Charset.defaultCharset()
	 */
	private static String arquivoParaString(String caminhoArquivo){
		try {	
			byte[] encoded = Files.readAllBytes(Paths.get(caminhoArquivo));
			return new String(encoded, StandardCharsets.UTF_8);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	private static String enveloparDummyEventos(String xml, String id) {
		xml = EsocialUtils.retirarDeclaracaoXml(xml);
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
		sb.append("         <evento Id=\"" + id + "\">");
		sb.append(xml);
		sb.append("         </evento>");
		sb.append("     </eventos>");
		sb.append("</envioLoteEventos>");
		sb.append("</eSocial>");

		return sb.toString();

	}

	

}
