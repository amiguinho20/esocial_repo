package util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 * 
 * @author pfugazza e Fábio<br/>
 * 
 * Diversas funções especiais sobre String 
 *
 */
public class Aux_String 
{
	private static DecimalFormat decimal = new DecimalFormat( "###,###,##0.00" );  
	/**
	 * Dado um <i>texto</i>, retornar parte deste levando em conta <i>delimitadorInicial</i>
	 * e <i>delimitadorFinal</i>.	
	 * @param texto
	 * @param delimitadorInicial
	 * @param delimitadorFinal
	 * @param entreDelimitadores 
	 * @return se <code>entreDelimitadores = true</code> : retornará os caracteres de <b>texto</b> entre <b>delimitadorInicial</b> e <b>delimitadorFinal</b>;</br>
	 *         se <code>entreDelimitadores = false</code> : retornará os caracteres de <b>texto</b> externos aos <b>delimitadorInicial</b> e <b>delimitadorFinal</b>;</br>
	 *         se <code>texto == null</code> : retornará vazio (<code>""</code>);</br> 
	 *         se <code>texto == ""</code> : retornará vazio (<code>""</code>). 
	 */
	public static String subStrIntoDelim(String texto, String delimitadorInicial, String delimitadorFinal, boolean entreDelimitadores)
	{
		StringBuilder textoValido = new StringBuilder();
		boolean encontrouPrimeiroDelimitador = false;
		try
		{
			for (int i=0; i < texto.length(); i++)
			{
				if ( encontrouPrimeiroDelimitador )
				{
					if ( ! texto.substring(i, i+1).equals(delimitadorFinal) )
						textoValido.append(texto.subSequence(i,i+1));
					else
						i = texto.length();					
				}
				else
				{
					if ( texto.substring(i,i+1).equals(delimitadorInicial) )
						encontrouPrimeiroDelimitador = true;
				}
			}
			if ( entreDelimitadores )
				return textoValido.toString();
			else
				return texto.replace(delimitadorInicial+textoValido+delimitadorFinal, "");
		}
		catch (NullPointerException npe)
		{
			return "";
		}
	}
	/**
	 * Dado um <i>texto</i>, retornar parte deste levando em conta <i>delimitadorInicial</i>
	 * e <i>delimitadorFinal</i>, a partir do último <i>delimitadorInicial</i>. 	
	 * @param texto
	 * @param delimitadorInicial Este parâmetro será ignorado e substituído pelo caracter <i>[</i>
	 * @param delimitadorFinal
	 * @param entreDelimitadores 
	 * @param aPartirDoUltimoDelimitadorInicial 
	 * @return se <code>entreDelimitadores = true</code> : retornará os caracteres de <b>texto</b> entre <b>delimitadorInicial</b> e <b>delimitadorFinal</b>;<br>
	 *         se <code>entreDelimitadores = false</code> : retornará os caracteres de <b>texto</b> externos aos <b>delimitadorInicial</b> e <b>delimitadorFinal</b> 
	 */
	public static String subStrIntoDelim(String texto, String delimitadorInicial, String delimitadorFinal, boolean entreDelimitadores, boolean aPartirDoUltimoDelimitadorInicial)
	{
		return inverte(subStrIntoDelim("["+inverte(texto),"[",delimitadorFinal,entreDelimitadores));
	}
	/**
	 * Dado um <i>texto</i>, retornar parte deste levando em conta os delimitadores <i>default</i> <b>[</b> e <b>]</b>. 
	 * @param texto
	 * @return retornará os caracteres de <b>texto</b> entre os delimitadores 
	 */
	public static String subStrIntoDelim(String texto)
	{
		return subStrIntoDelim(texto,"[","]",true);
	}
	/**
	 * @param string
	 * @param tamanho
	 * @return <code>string.substring(0,tamanho)</code></br>
	 * 
	 * Retornar <b>string</b> com a quantidade <b>tamanho</b> de caracteres. Será preenchido
	 * com espaços a direita ou será truncado dependendo do <b>tamanho</b>.
	 */
	public static String StrNesteTamanho(String string, int tamanho)
	{
		return (string+replicate(" ",tamanho)).substring(0, tamanho);
	}
	/**
	 * Dado um <i>numero</i>, converter num <i>String</i> preenchendo com <i>zero(s)</i>
	 * a esquerda levando em conta <i>tamanho</i>
	 * @param numero -> <i>int</i>eiro a ser convertido
	 * @param tamanho -> tamanho do <i>String</i> a ser retornado 
	 * @return
	 */
	public static String StrZeroNesteTamanho(int numero, int tamanho)
	{
		String strNumero = Integer.toString(numero);
		String complemento = replicate("0",tamanho-strNumero.length());
		return (complemento+strNumero);
	}
	/**
	 * @param caracter
	 * @param quantos
	 * @return <code>replicate(caracter,quantos)</code><br>
	 * Retornará um <i>string</i> de tamanho <b>quantos</b> composto da seqüência em <b>
	 * caracter</b>. 
	 */
	public static String replicate(String caracter, int quantos)
	{
		StringBuilder str=new StringBuilder();
		for (int i = 0; i < quantos; i++)
		{
			str.append(caracter);
		}
		return str.toString();
	}
	/**
	 * Similar à função Transf(...) do Clipper.</br>
	 * Transformar um <i>texto</i> num formato definido por uma <i>mascara</i>
	 * @param texto -> texto a ser transformado
	 * @param mascara -> mascara de transformação
	 * @return Exemplos: </br>transforma("123456","99999-9") retornará 12345-6</br>
	 *                   transforma("123456","99.999-9") retornará 12.345-6</br>
	 *                   transforma("1234","99.999-9") retornará 00.123-4 com zeros a esquerda</br>
	 *                   transforma("1234","XX.XXX-X") retornará    123-4 com espaços a esquerda
	 */
	public static String transforma(String texto, String mascara)
	{
		StringBuilder resultado=new StringBuilder();
		int tamMascara = mascara.length()-1;
		int tamTexto = texto.length()-1;
		while ( tamMascara > -1 )
		{
			if ( "() .-,/".contains(mascara.substring(tamMascara, tamMascara+1)) )
			{
				resultado.append(mascara.substring(tamMascara, tamMascara+1));
				tamMascara--;
			}
			else
			{
				if ( tamTexto > -1 )
				{
					resultado.append(texto.substring(tamTexto, tamTexto+1));
					tamTexto--;
				}
				else
				{
					resultado.append(mascara.substring(tamMascara, tamMascara+1).equals("9") ? "0":" ");
				}	
				tamMascara--;
			}
		}
		return inverte(resultado.toString());
	}
	/**
	 * Transformar um texto no seu inverso. Exemplo: <i>123456</i> será transformado em <i>654321</i>
	 * @param texto
	 * @return
	 */
	public static String inverte(String texto)
	{
		StringBuilder resultado=new StringBuilder();
		for (int i = texto.length()-1; i > -1; i--)
			resultado.append(texto.substring(i, i+1));

		return resultado.toString();
	}
	/**
	 * Similar à função StrTran(...) do Clipper
	 * @param parametro -> texto original
	 * @param existe -> caracter a ser pesquisado e substituido caso encontrado no texto original
	 * @param sera -> caracter que substituirá o caracter pesquisado 
	 * @return
	 */
	public static String modificaChar(String parametro, char existe, char sera) 
	{
		StringBuilder resultado = new StringBuilder();
		for ( int i=0 ; i<parametro.length() ; i++)
		{
			if ( existe == parametro.charAt(i) )
				resultado.append(String.valueOf(sera));
			else
				resultado.append(String.valueOf(parametro.charAt(i)));
		}
		return resultado.toString();
	}
	/**
	 * Converter parâmetro </i>String</i> recebido com a primeira letra em maiúscula e as demais em minúsculas 
	 * @param texto
	 * @return <i>texto</i> recebido como parâmetro, totalmente minúscula com a 1ª letra maiúscula
	 */
	public static String proper( String texto)
	{
		return texto.substring(0,1).toUpperCase().concat(texto.substring(1).toLowerCase());
	}
	/**
	 * Retornar de <i>meuTexto</i> somente os caracteres que fazem parte de <i>esteConjunto</i>
	 * @param meuTexto
	 * @param esteConjunto um conjunto de caracteres quaisquer e pode ser também:<li>
	 * <b>soDigitosNumericos</b> e então terá os dígitos 0123456789;<li>
	 * <b>soLetrasMaiusculas</b> e terá o conjunto ABCDEFGHIJKLMNOPQRSTUWXYZ;<li>
	 * <b>soLetrasMinusculas</b> e terá o conjunto abcdefghijklmnopqrstuwxyz;<li>
	 * <b>nomeDeArquivo</b> e terá o conjunto de carácteres válidos para nome de arquivos;
	 * @return
	 */
	public static String soCaracteres(String meuTexto, String esteConjunto)
	{
		StringBuilder retorno = new StringBuilder(); 
		String ec = qualConjunto(esteConjunto);
		for (int i = 0; i < meuTexto.length(); i++)
		{
			if (ec.contains(meuTexto.substring(i,i+1)) )
				retorno.append(meuTexto.substring(i,i+1));
		}
		return retorno.toString();
	}

	public static String qualConjunto(String esteConjunto)
	{
		String retorno;
		if (esteConjunto.equals("soDigitosNumericos"))
			retorno = "0123456789";
		else if (esteConjunto.equals("soLetrasMaiusculas"))
			retorno = "ABCDEFGHIJKLMNOPQRSTUWXYZ";
		else if (esteConjunto.equals("soLetrasMinusculas"))
			retorno = "abcdefghijklmnopqrstuwxyz";
		else if (esteConjunto.equals("nomeDeArquivo"))
			retorno = "0123456789ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuwxyz_.";
		else if (esteConjunto.equals("extensaoDeArquivo"))
			retorno = "0123456789ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuwxyz_";
		else
			retorno = esteConjunto;

		return retorno;
	}
	/**
	 * Transformar <i>String</i> em <i>int</i>, sem que retorne a exceção, caso ocorra.
	 * @param estesCaracteres
	 * @return <i>estesCaracteres</i> em <i>int</i> ou, se ocorrer <i>NumberFormatException</i>, 0
	 */
	public static int toInt(String estesCaracteres)
	{
		int numero = 0;
		try
		{
			numero = Integer.parseInt(estesCaracteres);
		}
		catch (NumberFormatException nfe)
		{
			numero = 0;
		}
		return numero;
	}
	/**
	 * Compara um texto qualquer (nome de arquivo, por exemplo) com a máscara fornecida.
	 * @param texto
	 * @param mascara no formato ????A?c??123?MA.???
	 * @return <i>true</i> se nome do arquivo está de acordo com a máscara; <i>false</i> caso contrário.
	 */
	public static boolean compara(String texto, String mascara) 
	{
		boolean ok = true;
		int indexador = 0;
		while (ok && indexador < mascara.length())
		{
			ok =( mascara.substring(indexador, indexador+1).equals("?") || mascara.substring(indexador, indexador+1).equals(texto.substring(indexador,indexador+1)) );
			indexador++;
		}
		return ok;
	}
	/**
	 * Compara um texto qualquer (nome de arquivo, por exemplo) com um conjunto de máscara fornecido.
	 * @param texto
	 * @param mascaras no formato ????A?c??123?MA.???
	 * @return <i>true</i> se nome do arquivo está de acordo com pelo menos uma das máscaras; <i>false</i> caso contrário.
	 */
	public static boolean compara(String texto, String[] mascaras) 
	{
		boolean ok = false;
		int i = 0;
		do 
		{
			ok = compara(texto,mascaras[i]);
			i++;
		} while (! ok && (i < mascaras.length));
		return ok;
	}
	/**
	 * Converter uma cadeia de caracteres codificada em UTF-8 para outra em ISO-8859-1
	 * @param str cadeia de caracteres a ser convertida
	 * @return cadeia de caracteres convertida para ISO-8859-1
	 */
	public static String UTF8toISO(String str)
	{
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());

        // decode UTF-8
        CharBuffer data = utf8charset.decode(inputBuffer);

        // encode ISO-8559-1
        ByteBuffer outputBuffer = iso88591charset.encode(data);
        byte[] outputData = outputBuffer.array();

        return new String(outputData);
    }
	/**
	 * Converter um valor qualquer num formato ###,###,##0.00
	 * @param valor
	 * @return valor formatado
	 */
	public static String formatoDecimal(Double valor) 
	{
		return decimal.format(valor);
	}

}
