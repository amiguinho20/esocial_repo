/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jvCert;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import util.Aux_String;
import util.LerStream;
/**
 * Classe copiada em 13/06/2017 do seguinte endere�o: 
 * http://www.guj.com.br/t/resolvido-como-selecionar-o-certificado-digital-token-e-assinar-arquivos-no-formato-pkcs7/182257/4
 * 
 * Fiz algumas adapta��es de acordo com nossas necessidades.
 * 
 * @author onvaid@hotmail.com
 * Soulslinux Onvaid Marques
 */
public class OnCert {
	//Procedimento que retorna o Keystore
	/*
	public static KeyStore funcKeyStore(String strAliasTokenCert) throws NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException 
	{
		String strResult = "";
		KeyStore ks = null;
		try 
		{
			ks = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
			ks.load(null, null);
			Enumeration<String> aliasEnum = ks.aliases();
			while (aliasEnum.hasMoreElements()) 
			{
				String aliasKey = (String) aliasEnum.nextElement();
				if (ks.isKeyEntry(aliasKey)) 
				{
					strResult = aliasKey;
				}
				if (ks.getCertificateAlias(ks.getCertificate(strResult)) == strAliasTokenCert) 
				{
					break;
				}
			}
		} 
		catch (KeyStoreException ex) 
		{
			System.out.println("ERROR " + ex.getMessage());
		}
		return ks;
	}
	*/
	
	public static KeyStore funcKeyStore(String caminhoCertificadoA1, String senhaA1) throws Exception{
		InputStream entrada = new FileInputStream(caminhoCertificadoA1);
		KeyStore ks = KeyStore.getInstance("pkcs12");
		try {
			ks.load(entrada, senhaA1.toCharArray());
		} catch (IOException e) {
			throw new Exception("Senha do Certificado Digital esta incorreta ou Certificado inválido.");
		}
		return ks;
	}
	
	//Procedimento de listagem dos certificados digitais
	public static String[] funcListaCertificados(boolean booCertValido) throws NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException 
	{
		//Estou setando a variavel para 20 dispositivos no maximo
		String strResult[] = new String[20];
		Integer intCnt = 0;
		try 
		{
			//KeyStore ks = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
			KeyStore ks = KeyStore.getInstance("KeychainStore");
			ks.load(null, null);
			Enumeration<String> aliasEnum = ks.aliases();
			while (aliasEnum.hasMoreElements()) 
			{
				String aliasKey = (String) aliasEnum.nextElement();
				if (booCertValido == false) 
				{
					strResult[intCnt] = aliasKey;
				} 
				else if (ks.isKeyEntry(aliasKey)) 
				{
					strResult[intCnt] = aliasKey;
				}
				if (strResult[intCnt] != null) 
				{
					intCnt = intCnt + 1;
				}
			}
		} 
		catch (KeyStoreException ex) 
		{
			System.out.println("ERROR " + ex.getMessage());
		}
		return strResult;
	}
	//Procedimento que retorna a chave privada de um certificado Digital
	public static PrivateKey funcChavePrivada(String strAliasTokenCert, String strAliasCertificado, String strArquivoCertificado, String strSenhaCertificado) throws Exception 
	{
		KeyStore ks = null;
		PrivateKey privateKey = null;
		if (strAliasTokenCert == null || strAliasTokenCert == "") 
		{
			ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(strArquivoCertificado);
			//Efetua o load do keystore
			ks.load(fis, strSenhaCertificado.toCharArray());
			//captura a chave privada para a assinatura
			privateKey = (PrivateKey) ks.getKey(strAliasCertificado, strSenhaCertificado.toCharArray());
		} 
		else 
		{
			if (strSenhaCertificado == null || strSenhaCertificado == "") 
			{
				strSenhaCertificado = "Senha";
			}
			//Procedimento para a captura da chave privada do token/cert
			
			ks = funcKeyStore(strAliasTokenCert, strSenhaCertificado);
			String alias = getAlias(ks);
			privateKey = (PrivateKey) ks.getKey(alias, strSenhaCertificado.toCharArray());
		}
		return privateKey;
	}
	//Procedimento que retorna a chave publica de um certificado Digital
	public static PublicKey funcChavePublica(String strAliasTokenCert, String strAliasCertificado, String strArquivoCertificado, String strSenhaCertificado) throws Exception 
	{
		KeyStore ks = null;
		PublicKey chavePublica = null;
		if (strAliasTokenCert == null || strAliasTokenCert == "") 
		{
			ks = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(strArquivoCertificado);
			//InputStream entrada para o arquivo
			ks.load(fis, strSenhaCertificado.toCharArray());
			fis.close();
//			Key chave = (Key) ks.getKey(strAliasCertificado, strSenhaCertificado.toCharArray());
			//O tipo de dado � declarado desse modo por haver ambig�idade (Classes assinadas com o mesmo nome "Certificate")
			java.security.Certificate cert = (java.security.Certificate) ks.getCertificate(strAliasCertificado);
			chavePublica = cert.getPublicKey();
		} 
		else 
		{
			if (strSenhaCertificado == null || strSenhaCertificado == "") 
			{
				strSenhaCertificado = "Senha";
			}
			//Procedimento se for utilizar token para a captura de chave publica
			ks = funcKeyStore(strAliasTokenCert, strSenhaCertificado);
//			Key key = ks.getKey(strAliasTokenCert, strSenhaCertificado.toCharArray());
			java.security.cert.Certificate crtCert = ks.getCertificate(strAliasTokenCert);
			chavePublica = crtCert.getPublicKey();
		}
		return chavePublica;
	}
	//Procedimento que verifica a assinatura
	public static boolean funcAssinaturaValida(PublicKey pbKey, byte[] bteBuffer, byte[] bteAssinado, String strAlgorithmAssinatura) throws Exception 
	{
		if (strAlgorithmAssinatura == null) 
		{
			strAlgorithmAssinatura = "MD5withRSA";
		}
		Signature isdAssinatura = Signature.getInstance(strAlgorithmAssinatura);
		isdAssinatura.initVerify(pbKey);
		isdAssinatura.update(bteBuffer, 0, bteBuffer.length);
		return isdAssinatura.verify(bteAssinado);
	}
	//Procedimento que gera a assinatura
	public static byte[] funcGeraAssinatura(PrivateKey pbKey, byte[] bteBuffer, String strAlgorithmAssinatura) throws Exception 
	{
		if (strAlgorithmAssinatura == null) 
		{
			strAlgorithmAssinatura = "MD5withRSA";
		}
		Signature isdAssinatura = Signature.getInstance(strAlgorithmAssinatura);
		isdAssinatura.initSign(pbKey);
		isdAssinatura.update(bteBuffer, 0, bteBuffer.length);
		return isdAssinatura.sign();
	}
	//Procedimento que retorna o status do certificado
	public static String funcStatusCertificado(X509Certificate crtCertificado) 
	{
		try 
		{
			crtCertificado.checkValidity();
			return "Certificado v�lido!";
		} 
		catch (CertificateExpiredException E) 
		{
			return "Certificado expirado!";
		} 
		catch (CertificateNotYetValidException E) 
		{
			return "Certificado inv�lido!";
		}
	}
	//Procedimento que retorna o certificado selecionado
	public static X509Certificate funcCertificadoSelecionado(String strAliasTokenCert, String strAliasCertificado, String strArquivoCertificado, String strSenhaCertificado) throws Exception 
	{
		X509Certificate crtCertificado = null;
		KeyStore crtRepositorio = null;
		if (strAliasTokenCert == null || strAliasTokenCert == "") 
		{
			//Procedimento de captura do certificao arquivo passado como parametro
			InputStream dado = new FileInputStream(strArquivoCertificado);
			crtRepositorio = KeyStore.getInstance("PKCS12");
			crtRepositorio.load(dado, strSenhaCertificado.toCharArray());
			crtCertificado = (X509Certificate) crtRepositorio.getCertificate(strAliasCertificado);
		} 
		else 
		{
			if (strSenhaCertificado == null || strSenhaCertificado == "") 
			{
				strSenhaCertificado = "Senha";
			}
			//Procedimento de captura do certificao token passado como parametro
			KeyStore.PrivateKeyEntry keyEntry;
			try 
			{
				KeyStore ks = funcKeyStore(strAliasTokenCert, strSenhaCertificado);				
				String alias = getAlias(ks);				
				keyEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(strSenhaCertificado.toCharArray()));
				//keyEntry = (KeyStore.PrivateKeyEntry) funcKeyStore(strAliasTokenCert, strSenhaCertificado).getEntry("mpt-cos-hom_mpt_mp_br3", new KeyStore.PasswordProtection(strSenhaCertificado.toCharArray()));
				crtCertificado = (X509Certificate) keyEntry.getCertificate();
			} 
			catch (KeyStoreException ex) 
			{
				Logger.getLogger(OnCert.class.getName()).log(Level.SEVERE, null, ex);
				//				ex.printStackTrace();
			}
		}
		return crtCertificado;
	}
	
	private static String getAlias(KeyStore ks) throws KeyStoreException{
		String alias = null;
		Enumeration<String> aliasesEnum = ks.aliases();
		while (aliasesEnum.hasMoreElements()) {
			alias = (String) aliasesEnum.nextElement();
			break;
			//System.out.println("alias: " + alias);
			//if (ks.isKeyEntry(alias)) {
			//	break;
			//}
		}
		return alias;
	}
	
/*	
	//Procedimento de Parametros de assinatura
	public static class TAssinaXML2
	{
		//MD2withRSA - MD5withRSA - SHA1withRSA - SHA224withRSA - SHA256withRSA - SHA1withDSA - DSA - RawDSA
		//public String strAlgorithmAssinatura = "MD5withRSA";
		public String strAliasTokenCert = null;
		public String strAliasCertificado = null;
		public String strArquivoCertificado = null;
		public String strSenhaCertificado = null;
		public String strArquivoXML = null;
		public String strArquivoSaveXML = null;
		public String C14N_TRANSFORM_METHOD = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
		public boolean booNFS = true;
		
	}
	//Procedimento de assinar XML
	public static boolean funcAssinaXML2(TAssinaXML tpAssinaXML) throws Exception 
	{
//		Signature sgi = null;
		XMLSignatureFactory sig = null;
		SignedInfo si = null;
		KeyInfo ki = null;
		String strTipoSign = "?";
		String strID = "Id";
		//
		// Ler o XML a fim de obter a tag do Id
		String linha;
		LerStream lerStream = new LerStream(tpAssinaXML.strArquivoXML);
		if ( lerStream.prontoParaLeitura() )
		{
			while ((linha = lerStream.proximaLinha()) != "{EOF}" && strTipoSign.equals("?")) 
			{
				if ( linha.contains(strID+"=") )
					strTipoSign = Aux_String.subStrIntoDelim(linha, "<", " ", true);
			}
			lerStream.close();
		}
		//Capturo o certificado
		X509Certificate cert = funcCertificadoSelecionado(tpAssinaXML.strAliasTokenCert, tpAssinaXML.strAliasCertificado, tpAssinaXML.strArquivoCertificado, tpAssinaXML.strSenhaCertificado);
		//Inicializo o arquivo/carrego
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(tpAssinaXML.strArquivoXML));
		sig = XMLSignatureFactory.getInstance("DOM");
		ArrayList<Transform> transformList = new ArrayList<Transform>();
		Transform enveloped = sig.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
		Transform c14n = sig.newTransform(tpAssinaXML.C14N_TRANSFORM_METHOD, (TransformParameterSpec) null);
		transformList.add(enveloped);
		transformList.add(c14n);
		NodeList elements = doc.getElementsByTagName(strTipoSign);
		org.w3c.dom.Element el = (org.w3c.dom.Element) elements.item(0);
//		String id = el.getAttribute(strID);
		String id = el.getAttribute("Id");
		el.setIdAttribute("Id", true);
		Reference r = sig.newReference("#".concat(id), sig.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null),
				transformList,
				null, null);
		
//        Reference r = sig.newReference("#".concat(id), sig.newDigestMethod(DigestMethod.SHA256, null),
//                transformList,
//                null, null);
//
//        si = sig.newSignedInfo(
//                sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
//                (C14NMethodParameterSpec) null),
//                sig.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
//                Collections.singletonList(r));
		      
		si = sig.newSignedInfo(
				sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
						(C14NMethodParameterSpec) null),
				sig.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null ),
				Collections.singletonList(r));

		KeyInfoFactory kif = sig.getKeyInfoFactory();
		List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
		x509Content.add(cert);
		X509Data xd = kif.newX509Data(x509Content);
		ki = kif.newKeyInfo(Collections.singletonList(xd));
		DOMSignContext dsc = new DOMSignContext(funcChavePrivada(tpAssinaXML.strAliasTokenCert, tpAssinaXML.strAliasCertificado, tpAssinaXML.strArquivoCertificado, tpAssinaXML.strSenhaCertificado), doc.getDocumentElement());
		XMLSignature signature = sig.newXMLSignature(si, ki);
		signature.sign(dsc);
		//Salvo o arquivo assinado
		OutputStream os = new FileOutputStream(tpAssinaXML.strArquivoSaveXML);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer();
		trans.transform(new DOMSource(doc), new StreamResult(os));
		return true;
	}
*/	
	
	
	public static String assinarXML(String xml, String caminhoCacert, String caminhoCertificadoA1, String senhaCertificadoA1) throws Exception{
		String xmlAssinado = null;
		
		final String C14N_TRANSFORM_METHOD = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
		
        XMLSignatureFactory sig = null;  
        SignedInfo si = null;  
        KeyInfo ki = null;  

        //Capturo o certificado  
        X509Certificate cert = carregarCertificado(caminhoCertificadoA1, senhaCertificadoA1);
        //Inicializo o arquivo/carrego  
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        dbf.setNamespaceAware(true);  
        
        //Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(tpAssinaXML.strArquivoXML));         
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document doc = dbf.newDocumentBuilder().parse(inputStream);
        
        sig = XMLSignatureFactory.getInstance("DOM");  
        ArrayList<Transform> transformList = new ArrayList<Transform>();  
        Transform enveloped = sig.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);  
        Transform c14n = sig.newTransform(C14N_TRANSFORM_METHOD, (TransformParameterSpec) null);  
        transformList.add(enveloped);  
        transformList.add(c14n);  
                                                                  
        Reference r = sig.newReference("", sig.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null), transformList, null, null);  
        si = sig.newSignedInfo(  
                sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,  
                        (C14NMethodParameterSpec) null),  
                sig.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null ),  
                Collections.singletonList(r));  
  
        KeyInfoFactory kif = sig.getKeyInfoFactory();  
        List<X509Certificate> x509Content = new ArrayList<X509Certificate>();  
        x509Content.add(cert);  
        X509Data xd = kif.newX509Data(x509Content);  
        ki = kif.newKeyInfo(Collections.singletonList(xd));  
        PrivateKey privateKey = carregarChavePrivada(caminhoCertificadoA1, senhaCertificadoA1);
        DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());  
        XMLSignature signature = sig.newXMLSignature(si, ki);  
        signature.sign(dsc);        // neste momento é solicitada a senha do token  
        
        
        xmlAssinado = documentParaString(doc);
		
		
		return xmlAssinado;
	}
	
	public static PrivateKey carregarChavePrivada(String caminhoCertificadoA1, String senhaCertificadoA1) throws Exception{
		KeyStore ks = null;
		PrivateKey privateKey = null;
		ks = funcKeyStore(caminhoCertificadoA1, senhaCertificadoA1);
		String alias = getAlias(ks);
		privateKey = (PrivateKey) ks.getKey(alias, senhaCertificadoA1.toCharArray());
		return privateKey;
	}
	
	
	public static X509Certificate carregarCertificado(String caminhoCertificadoA1, String senhaCertificadoA1) throws Exception 
	{
		X509Certificate crtCertificado = null;
		KeyStore.PrivateKeyEntry keyEntry;
		try 
		{
			KeyStore ks = funcKeyStore(caminhoCertificadoA1, senhaCertificadoA1);				
			String alias = getAlias(ks);				
			keyEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(senhaCertificadoA1.toCharArray()));
			crtCertificado = (X509Certificate) keyEntry.getCertificate();
		} 
		catch (KeyStoreException ex) 
		{
			throw ex;
		}
		return crtCertificado;
	}

	
	public static String documentParaString(Document doc) throws TransformerException{
		StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
	}
	
	
    public static class TAssinaXML   
    {  
        //MD2withRSA - MD5withRSA - SHA1withRSA - SHA224withRSA - SHA256withRSA - SHA1withDSA - DSA - RawDSA  
        //public String strAlgorithmAssinatura = "MD5withRSA";  
        public String strAliasTokenCert = null;  
        public String strAliasCertificado = null;  
        public String strArquivoCertificado = null;  
        public String strSenhaCertificado = null;  
        public String strArquivoXML = null;  
        public String strArquivoSaveXML = null;  
        public String C14N_TRANSFORM_METHOD = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";  
        public boolean booNFS = true;  
        public String xmlAssinado;  
        public String idEvento = "";  
        public String nrInscEmpregador = "?";  
    }  
    //Procedimento de assinar XML  

    public static boolean funcAssinaXML(TAssinaXML tpAssinaXML) throws Exception   
    {  
//      Signature sgi = null;  
        XMLSignatureFactory sig = null;  
        SignedInfo si = null;  
        KeyInfo ki = null;  
        String strTipoSign = "?";  
        String strID = "Id";  
        //  
        // Ler o XML a fim de obter a tag do Id do evento e a inscrição do empregador  
        String linha;  
        LerStream lerStream = new LerStream(tpAssinaXML.strArquivoXML);  
        if ( lerStream.prontoParaLeitura() )  
        {  
            while ((linha = lerStream.proximaLinha()) != "{EOF}" && tpAssinaXML.nrInscEmpregador.equals("?"))   
            {  
                if ( linha.contains(strID+"=") )  
                {  
                    strTipoSign = Aux_String.subStrIntoDelim(linha, "<", " ", true);  
                    tpAssinaXML.idEvento = Aux_String.subStrIntoDelim(linha," ",">",true);  
                    System.out.println("strTipoSing.: " + strTipoSign);
                    System.out.println("idEvento   .: " + tpAssinaXML.idEvento);
                }  
                else if ( linha.contains("<nrInsc>"))  
                {  
                    tpAssinaXML.nrInscEmpregador = Aux_String.subStrIntoDelim(linha,">","<",true);  
                    System.out.println("nrInscEmpregador.: " + tpAssinaXML.nrInscEmpregador);
                }  
            }  
            lerStream.close();  
        }  
        //Capturo o certificado  
        X509Certificate cert = funcCertificadoSelecionado(tpAssinaXML.strAliasTokenCert, tpAssinaXML.strAliasCertificado, tpAssinaXML.strArquivoCertificado, tpAssinaXML.strSenhaCertificado);  
        //Inicializo o arquivo/carrego  
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        dbf.setNamespaceAware(true);  
        Document doc = dbf.newDocumentBuilder().parse(new FileInputStream(tpAssinaXML.strArquivoXML));  
        sig = XMLSignatureFactory.getInstance("DOM");  
        ArrayList<Transform> transformList = new ArrayList<Transform>();  
        Transform enveloped = sig.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);  
        Transform c14n = sig.newTransform(tpAssinaXML.C14N_TRANSFORM_METHOD, (TransformParameterSpec) null);  
        transformList.add(enveloped);  
        transformList.add(c14n);  
        NodeList elements = doc.getElementsByTagName(strTipoSign);  
        org.w3c.dom.Element el = (org.w3c.dom.Element) elements.item(0);  
//      String id = el.getAttribute(strID);  
        String id = el.getAttribute("Id");  
        el.setIdAttribute("Id", true);  
                                                                  
        Reference r = sig.newReference("", sig.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null), transformList, null, null);  
        si = sig.newSignedInfo(  
                sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,  
                        (C14NMethodParameterSpec) null),  
                sig.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null ),  
                Collections.singletonList(r));  
  
        KeyInfoFactory kif = sig.getKeyInfoFactory();  
        List<X509Certificate> x509Content = new ArrayList<X509Certificate>();  
        x509Content.add(cert);  
        X509Data xd = kif.newX509Data(x509Content);  
        ki = kif.newKeyInfo(Collections.singletonList(xd));  
        PrivateKey privateKey = funcChavePrivada(tpAssinaXML.strAliasTokenCert, tpAssinaXML.strAliasCertificado, tpAssinaXML.strArquivoCertificado, tpAssinaXML.strSenhaCertificado);
        DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());  
        XMLSignature signature = sig.newXMLSignature(si, ki);  
        signature.sign(dsc);        // neste momento é solicitada a senha do token  
        
        
        TransformerFactory tf = TransformerFactory.newInstance(); 
        //  
// descomente o código abaixo caso queira salvar o XML assinado  
     
        OutputStream os = new FileOutputStream(tpAssinaXML.strArquivoSaveXML);    //Salvo o XML assinado num arquivo  
        //TransformerFactory tf = TransformerFactory.newInstance(); 
        Transformer trans = tf.newTransformer(); 
        trans.transform(new DOMSource(doc), new StreamResult(os)); 
        
        StringWriter writer = new StringWriter();                                 //Salvo o XML assinado numa propriedade String  
        Transformer transformer = tf.newTransformer();  
        transformer.transform(new DOMSource(doc), new StreamResult(writer));  
        tpAssinaXML.xmlAssinado = writer.getBuffer().toString();  
          
        return true;  
    }  
	
/*
	O m�todo abaixo foi retirado deste endere�o : https://stackoverflow.com/questions/12528667/xml-digital-signature-java
	para futuros testes.
	
	private static Document sign(Document doc) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException, MarshalException, XMLSignatureException,
	FileNotFoundException, TransformerException 
	{

		String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

		DigestMethod digestMethod = fac.newDigestMethod(DigestMethod.SHA256, null);
		Transform transform = fac.newTransform(ENVELOPED, (TransformParameterSpec) null);
		Reference reference = fac.newReference("", digestMethod, singletonList(transform), null, null);
		SignatureMethod signatureMethod = fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
		CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod(EXCLUSIVE, (C14NMethodParameterSpec) null);

		// Create the SignedInfo
		SignedInfo si = fac.newSignedInfo(canonicalizationMethod, signatureMethod, singletonList(reference));


		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);

		KeyPair kp = kpg.generateKeyPair();

		KeyInfoFactory kif = fac.getKeyInfoFactory();
		KeyValue kv = kif.newKeyValue(kp.getPublic());

		// Create a KeyInfo and add the KeyValue to it
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
		DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement());

		XMLSignature signature = fac.newXMLSignature(si, ki);
		signature.sign(dsc);

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer();

		// output the resulting document
		OutputStream os;

		os = new FileOutputStream("xmlOut.xml");

		trans.transform(new DOMSource(doc), new StreamResult(os));
		return doc;
	}    
*/
}