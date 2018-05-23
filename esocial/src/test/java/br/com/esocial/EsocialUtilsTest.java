package br.com.esocial;

import org.junit.Test;
import org.junit.Assert;

public class EsocialUtilsTest {
	
	@Test
	public void xpathFinder() {
		
		String xml = "<carro><modelo>mustang</modelo><marca>ford</marca></carro>";
		String retorno = EsocialUtils.xpathFinder(xml, "/carro/modelo/text()");
		
		Assert.assertEquals("mustang", retorno);
		
	}

	
	@Test
	public void retirarDeclaracaoXml() {
		
		String xml = "<?xml bla bla bla ?><carro><modelo>mustang</modelo><marca>ford</marca></carro>";
		String retorno = EsocialUtils.retirarDeclaracaoXml(xml);
		
		Assert.assertTrue(xml.contains("<?xml"));
		Assert.assertFalse(retorno.contains("<?xml"));
		
	}

	
	
}
