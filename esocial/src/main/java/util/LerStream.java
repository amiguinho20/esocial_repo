package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
/**
 * 
 * @author pfugazza
 *
 * <p>Ler arquivos-texto a partir do contexto da aplicação (via <code>InputStream</code>) ou a partir
 * de um arquivo fora do contexto da aplicação (via <code>FileReader</code>)
 */
public class LerStream 
{
	private	InputStream is = null;
	private FileReader fileMI = null;
	private BufferedReader br;
	private String linha = "{OK}";
	/**
	 * 
	 * @return <b>{OK}</b> se o arquivo está pronto para leitura;<br>
	 * <b>{FileNotFoundException}</b> se o arquivo não foi encontrado;<br>
	 * <b>{null}</b> se o arquivo não puder ser lido do contexto da aplicação.
	 */
	private String getLinha() 
	{
		return linha;
	}
	public boolean prontoParaLeitura()
	{
		return getLinha().equals("{OK}");
	}
	public boolean arquivoNaoEncontrado()
	{
		return getLinha().equals("{null}");
	}
	/**
	 * 
	 * @param fileName - nome do arquivo no formato u:\path\nomeDoArquivo.extensão
	 * <p>Ler arquivo-texto fora do contexto da aplicação (via <code>InputStream</code>)
	 * <p>Se arquivo não encontrado em <code>getLinha()</code> estará o conteúdo 
	 * "<b>{FileNotFoundException}</b>", caso contrário este conteúdo sera "<b>{OK}</b>".
	 */
	public LerStream(String fileName)
	{
		try 
		{
			fileMI = new FileReader(fileName);
			br = new BufferedReader(fileMI);
		} 
		catch (FileNotFoundException e) 
		{
			linha = "{FileNotFoundException}";    // arquivo não encontrado
		}
	}
	/**
	 * 
	 * @return conteúdo da linha lida. Se nada mais existir para ser lido, <i>proximaLinha()</i> 
	 * retornará o string <b>{EOF}</b> e, neste caso, executará o método <i>close()</i>. 
	 */
	public String proximaLinha()
	{
		try 
		{
			linha = br.readLine();
			if ( linha == null )
			{
				linha = "{EOF}";                  // fim de arquivo
				close();
			}
			else if ( linha.startsWith("*"))      // comentário
				linha = proximaLinha();
		} 
		catch (IOException e) 
		{
			linha = "{IOException}";              // fim de arquivo
			e.printStackTrace();
		}
		return linha;
	}
	/**
	 * Método para fechar o <i>BufferedReader</i> e o <i>FileReader</i><P>
	 * Obs. chamá-lo se não deixar ir até a leitura da última linha do arquivo, ou seja,
	 * se <i>proximaLinha()</i> não retornar <b>{EOF}</b>.
	 */
	public void close() 
	{
		try
		{
			br.close();
			if ( fileMI != null )
				fileMI.close();
			if ( is != null)
				is.close();
		}
		catch (IOException e) 
		{
			linha = "{IOException}";              // fim de arquivo
		}
		catch (NullPointerException npe)
		{
			linha = "{NullPointerException}";
		}

	}
	/**
	 * Irá retornar a primeira linha que iniciar pelo caracter <b>[</b>. Geralmente lida do arquivo <b>MensagemGlobal.Status</b>.</br>
	 * Irá preencher, também, o <i>ArrayList</i> <i>cores</i> com os códigos de cores que serão usadas para exibir o item de menu
	 * <b>Mensagens</b>. Estes códigos estão caracterizados por ter o caracter <b>#</b> no início de cada linha. Se nenhuma cor
	 * for fornecida, assumirá uma default.
	 * @return
	 */
	public String getMensagem(ArrayList<String> cores) 
	{
		String msgMI = "[?]Nada tenho a lhe dizer neste momento, ok?";
		if (prontoParaLeitura())
		{
			while ( ( (linha = proximaLinha()) != "{EOF}" ) && (msgMI.startsWith("[?]")) )
			{
				if ( linha.startsWith("[") )
					msgMI = linha;
				else if ( linha.startsWith("#") )
					cores.add(linha);
			}
		}
		close();
		if (cores.size() == 0)
			cores.add("#ff6820");
		
		return msgMI;
	}
	
}

