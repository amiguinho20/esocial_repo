package br.com.esocial;

import java.io.FileNotFoundException;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.exception.CertificadoException;


/**
 * 
 * @author eduardo.csilva 
 *
 * Dependencia: 
 *  1) um arquivo "cacaerts" customizado com os enderecos de dominios dos webservices \n
 *     @see http://www.javac.com.br/jc/posts/list/2736-arquivo...reinf-atualizado-22042018.page
 *     @see https://github.com/Samuel-Oliveira/Java_Certificado/blob/master/src/main/java/br/com/samuelweb/certificado/util/CacertUtil.java 
 *  2) um certificado digital (A1 ou A3) do tipo e-CPF ou e-CNPJ, com raiz do ICP Brasil, para assinatura de eventos e comunicação segura com o webservice.
 *  3) eventualmente, podera ser necessario a instalacao dos certificados 3 ultimos certificados do ICP Brasil: https://certificados.serpro.gov.br/serproacf/certificate-chain
 *
 */
public class ConsultarXml {
	
	private static final String END_POINT = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/consultarloteeventos/WsConsultarLoteEventos.svc";
	
	private Certificado certificado;

	/**
	 * Uso com o certificado A1
	 * @param caminhoCacerts
	 * @param caminhoCertificadoA1
	 * @param senhaCertificadoA1
	 */
	public ConsultarXml(String caminhoCacerts, String caminhoCertificadoA1, String senhaCertificadoA1) {
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
	public ConsultarXml(String caminhoCacerts, String senhaCertificadoA3) {
		try {
			certificado = EsocialUtils.getCertificadoA3(senhaCertificadoA3);
			EsocialUtils.inicializarCertificadSocketDinamico(caminhoCacerts, certificado);
		} catch (CertificadoException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void main(String[] args) {
		
		String caminhoCacerts = "C:/desenv/certificados/cacert/Cacert-22-04-2018";
//		String caminhoCacerts = "C:/desenv/jdk/jdk-8u172x64/jre/lib/security/cacerts";
		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = args[0];		
		String senhaCertificadoA3 = args[1];
		
		ConsultarXml consultarXml = new ConsultarXml(caminhoCacerts, caminhoCertificadoA1, senhaCertificadoA1);

		String protocoloEnvio = "1.2.201805.0000000000004781215";
		
		String retorno = consultarXml.consultar(protocoloEnvio);
		
		System.out.println(
				"\n\nRETORNO DA CONSULTA DO ESOCIAL \n");
		System.out.println(EsocialUtils.formatarXml(retorno, false));
	}
	
	public String consultar(String protocolo) {
		String xml = enveloparDummyConsulta(protocolo);
		String xmlEnvelopado = EsocialUtils.enveloparSoap(xml);
		String retorno = EsocialUtils.chamarConsultarWs(xmlEnvelopado, END_POINT);
		return retorno;
	}
	
	
	private static String enveloparDummyConsulta(String protocoloEnvio) {
		StringBuilder sb = new StringBuilder();
		sb.append("<eSocial xmlns=\"http://www.esocial.gov.br/schema/lote/eventos/envio/consulta/retornoProcessamento/v1_0_0\">");
		sb.append("<consultaLoteEventos>");
		sb.append("<protocoloEnvio>");
		sb.append(protocoloEnvio);
		sb.append("</protocoloEnvio>");
		sb.append("</consultaLoteEventos>");
		sb.append("</eSocial>");
		return sb.toString();
	}



}
