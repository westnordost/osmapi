package de.westnordost.osmapi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

/** Writes the payload for a multipart/form-data form.<br/>
 *  Override {@link #write()} and add your form fields with {@link #addField(String, String)} and
 *  {@link #addFileField(String, String, ApiRequestWriter)} there. */
public abstract class FormDataWriter implements ApiRequestWriter
{
	private static final String CHARSET = "UTF-8";
	private static final String LINE_FEED = "\r\n";
	
	final String boundary;
	
	private PrintWriter printer;
	private OutputStream out;
	
	public FormDataWriter()
	{
		boundary = "---------------------------" + System.currentTimeMillis();
	}
	
	@Override
	public String getContentType()
	{
		return "multipart/form-data; boundary=" + boundary;
	}

	@Override
	public final void write(OutputStream out) throws IOException
	{
		printer = new PrintWriter(new OutputStreamWriter(out, CHARSET));
		this.out = out;
		write();
		finish();
	}
	
	protected abstract void write() throws IOException;
	
	private void finish()
	{
		printer.flush();
		println("--" + boundary + "--");
		printer.close();
	}
	
	protected final void addField(String name, String value)
	{
		println("--" + boundary);
		println("Content-Disposition: form-data; name=\"" + name + "\"");
		println("Content-Type: text/plain; charset="+CHARSET.toLowerCase(Locale.UK));
		println();
		println(value);
		printer.flush();
	}
	
	protected final void addFileField(String name, String fileName, ApiRequestWriter subWriter)
			throws IOException
	{
		println("--" + boundary);
		println("Content-Disposition: form-data; name=\"" + name + "\"; " +
				"filename=\"" + fileName + "\"");
		println("Content-Type: " + subWriter.getContentType());
		println();
		printer.flush();
		subWriter.write(out);
		println();
		printer.flush();
	}
		
	private void println()
	{
		printer.append(LINE_FEED);
	}
	
	private void println(CharSequence text)
	{
		printer.append(text);
		println();
	}
	
}
