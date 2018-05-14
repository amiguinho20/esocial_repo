package br.com.esocial;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class EnviarEsocialServiceClient {

	private static final String ESOCIAL_ENVIAR_ENDPOINT = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc";
	
	private static final String ESOCIAL_CONSULTAR_ENDPOINT = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/consultarloteeventos/WsConsultarLoteEventos.svc";
	//coloque o local onde esta sua chave privada
	private static final String PRIVATE_KEY = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";
	private static final String PRIVATE_KEY_PWD = "mpt";
	//coloque o local onde esta sua chave publica que é uma keystore contendo os 3 certificados, icp-brasil, serpro v4 e serprov5final
	private static final String PUBLIC_KEY = "/Users/Amiguinho/Development/tmp/esocial/Cacert-22-04-2018_2";
	private static final String PUBLIC_KEY_PWD = "changeit";

	public static String enviarLotes(final String envelopeSoap12) {

		HttpsURLConnection con = null;
		DataOutputStream stream = null;
		String resultESocial = null;

		try {
			
			URL endPoint = new URL(null, ESOCIAL_ENVIAR_ENDPOINT, new sun.net.www.protocol.https.Handler());
			//carrega a chave privada
			final KeyStore keystore = getKeystoreByName("PKCS12", PRIVATE_KEY, PRIVATE_KEY_PWD);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyManager[] keyManagers;
			kmf.init(keystore, PRIVATE_KEY_PWD.toCharArray());
			keyManagers = kmf.getKeyManagers();
			
			//carrega a chave publica
			final KeyStore trustStore = getTrustStoreByName("JKS", PUBLIC_KEY, PUBLIC_KEY_PWD);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
			SSLContext ctx = SSLContext.getInstance("TLSv1.2");
			ctx.init(keyManagers, tmf.getTrustManagers(), new java.security.SecureRandom());
			SSLSocketFactory sslSocketFactory = ctx.getSocketFactory();

			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			con = (HttpsURLConnection) endPoint.openConnection();
			con.setSSLSocketFactory(sslSocketFactory);
			con.setHostnameVerifier(hostnameVerifier);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			con.setRequestProperty("Content-Length",  String.valueOf(URLEncoder.encode(envelopeSoap12, "UTF-8").length()));
			//copiar o endereço da soapAction que fica dentro do wsdl
			con.setRequestProperty("soapAction", "http://www.esocial.gov.br/servicos/empregador/lote/eventos/envio/v1_1_0/ServicoEnviarLoteEventos/EnviarLoteEventos");
			con.setConnectTimeout(30000);

			stream = new DataOutputStream(con.getOutputStream());
			stream.write(envelopeSoap12.getBytes("UTF-8"));
			stream.flush();
			// Connect
			con.connect();
			resultESocial = inputStreamToString(con.getInputStream());
			System.out.println("Retorno Vamo lá: " + resultESocial);
			// Release
			con.disconnect();
		} catch (final Exception ex) {
			ex.printStackTrace();

		} finally {
			try {

				if (stream != null) {
					stream.close();
					stream = null;
				}

				if (con != null) {
					con.disconnect();
					con = null;
				}

			} catch (final Exception e) {
				// Debug.log("callSefaz # ", Debug.ERROR, , e);
			}
		}

		return resultESocial;
	}	
	
	public static String consultarLotes(final String envelopeSoap12) {

		HttpsURLConnection con = null;
		DataOutputStream stream = null;
		String resultESocial = null;

		try {
			
			URL endPoint = new URL(null, ESOCIAL_CONSULTAR_ENDPOINT, new sun.net.www.protocol.https.Handler());
			//carrega a chave privada
			final KeyStore keystore = getKeystoreByName("PKCS12", PRIVATE_KEY, PRIVATE_KEY_PWD);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyManager[] keyManagers;
			kmf.init(keystore, PRIVATE_KEY_PWD.toCharArray());
			keyManagers = kmf.getKeyManagers();
			
			//carrega a chave publica
			final KeyStore trustStore = getTrustStoreByName("JKS", PUBLIC_KEY, PUBLIC_KEY_PWD);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);
			SSLContext ctx = SSLContext.getInstance("TLSv1.2");
			ctx.init(keyManagers, tmf.getTrustManagers(), new java.security.SecureRandom());
			SSLSocketFactory sslSocketFactory = ctx.getSocketFactory();

			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			con = (HttpsURLConnection) endPoint.openConnection();
			con.setSSLSocketFactory(sslSocketFactory);
			con.setHostnameVerifier(hostnameVerifier);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			con.setRequestProperty("Content-Length",  String.valueOf(URLEncoder.encode(envelopeSoap12, "UTF-8").length()));
			//copiar o endereço da soapAction que fica dentro do wsdl
			con.setRequestProperty("soapAction", "http://www.esocial.gov.br/servicos/empregador/lote/eventos/envio/consulta/retornoProcessamento/v1_1_0/ServicoConsultarLoteEventos/ConsultarLoteEventos");
			con.setConnectTimeout(30000);

			stream = new DataOutputStream(con.getOutputStream());
			stream.write(envelopeSoap12.getBytes("UTF-8"));
			stream.flush();
			// Connect
			con.connect();
			resultESocial = inputStreamToString(con.getInputStream());
			System.out.println("Retorno Vamo lá: " + resultESocial);
			// Release
			con.disconnect();
		} catch (final Exception ex) {
			ex.printStackTrace();

		} finally {
			try {

				if (stream != null) {
					stream.close();
					stream = null;
				}

				if (con != null) {
					con.disconnect();
					con = null;
				}

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		return resultESocial;
	}

	/**
	 * converte o stream de retorno em String
	 * */
	private static String inputStreamToString(final InputStream is)
			throws IOException {

		if (is != null) {

			final Writer writer = new StringWriter();
			final char[] buffer = new char[2048];

			try {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;

				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		}
		return "";
	}

	/**
	 * carrega a chave privada
	 * */
	public static KeyStore getKeystoreByName(String ksType, String store, String pwd) throws Exception {

		KeyStore ks = KeyStore.getInstance(ksType);
		InputStream entrada = new FileInputStream(store);
		ks.load(entrada, pwd.toCharArray());
		entrada.close();
		return ks;
	}
	
	/**
	 * carrega a chave publica
	 * */
	public static KeyStore getTrustStoreByName(String ksType, String store, String pwd) throws Exception{
		
		KeyStore ks = KeyStore.getInstance(ksType);
		InputStream entrada = new FileInputStream(store);
		ks.load(entrada, pwd.toCharArray());
		entrada.close();
		return ks;
	}
	
	public static String enveloparSoap(String xml){
		StringBuilder sb = new StringBuilder();		
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("                  xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" "); 
		sb.append("                  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "); 
		sb.append("                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("                  xmlns:esocial=\"http://www.esocial.gov.br/servicos/empregador/lote/eventos/envio/v1_1_0\"> ");
		sb.append("   <soapenv:Header/> " );
		sb.append("   <soapenv:Body>  ");
		sb.append("      <esocial:EnviarLoteEventos> "); 
		sb.append("         <esocial:loteEventos>  ");
		sb.append(xml);
		sb.append("         <esocial:loteEventos/> " );  
		sb.append("      </esocial:EnviarLoteEventos> ");  
		sb.append("   </soapenv:Body> ");
		sb.append("</soapenv:Envelope> ");
		return sb.toString();
	}
	
}
