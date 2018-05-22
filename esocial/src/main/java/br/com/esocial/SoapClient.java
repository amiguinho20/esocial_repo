package br.com.esocial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @see https://github.com/thiagovespa/gvlabs-soap-utils/blob/master/gvlabs-soap-utils/src/org/gvlabs/utils/soap/SoapClient.java
 * @see https://thiagovespa.com.br/blog/2012/12/12/chamando-servicos-soap-sem-proxy-stubs-e-afins/
 */
public class SoapClient {

	private String endpoint;

	/**
	 * Constructor with an endpoint
	 * 
	 * @param endpoint
	 *            soap endpoint
	 */
	public SoapClient(String endpoint) {
		super();
		this.endpoint = endpoint;
	}

	/**
	 * Invoke a SOAP Operation
	 * 
	 * @param operation
	 *            operation name (Header: SOAPAction)
	 * @param input
	 *            request payload
	 * @return response payload
	 * @throws SOAPException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public String invokeOperation(String operation, String input)
			throws SOAPException, ParserConfigurationException, SAXException,
			IOException, TransformerException {
		String response = null;
		SOAPConnection connection = null;
		try {
			// Connection
			SOAPConnectionFactory soaConnFactory = getSoaConnFactory();
			connection = soaConnFactory.createConnection();

			// Message
			MessageFactory messageFactory = getMessageFactory();
			SOAPMessage message = messageFactory.createMessage();

			// Header
			MimeHeaders headers = message.getMimeHeaders();
			headers.addHeader("SOAPAction", operation);

			// Body
			SOAPBody body = message.getSOAPBody();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			DocumentBuilder parser = dbf.newDocumentBuilder();
			if (input != null) {
				StringBuffer inputData = new StringBuffer(input);
				ByteArrayInputStream bis = new ByteArrayInputStream(inputData
						.toString().getBytes("UTF-8"));
				Document xmlDoc = parser.parse(bis);

				xmlDoc.setStrictErrorChecking(false);
				body.addDocument(xmlDoc);
			}

			message.saveChanges();

			// Call a operation
			SOAPMessage soapResponse = connection.call(message, new URL(
					endpoint));

			if (soapResponse != null) {
				// Get response
				TransformerFactory tff = TransformerFactory.newInstance();
				Transformer tf = tff.newTransformer();

				Source sc = soapResponse.getSOAPPart().getContent();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamResult result = new StreamResult(baos);
				tf.transform(sc, result);
				response = baos.toString("UTF-8");
			}

		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return response;
	}

	protected MessageFactory getMessageFactory() throws SOAPException {
		return MessageFactory.newInstance();
	}

	protected SOAPConnectionFactory getSoaConnFactory() throws SOAPException {
		return SOAPConnectionFactory.newInstance();
	}
}

