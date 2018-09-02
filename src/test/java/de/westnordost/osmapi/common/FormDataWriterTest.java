package de.westnordost.osmapi.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.TestUtils;
import junit.framework.TestCase;

public class FormDataWriterTest extends TestCase
{
	public void testOneField() throws IOException
	{
		Map<String,String> params = new HashMap<>();
		params.put("TestXY", "one two three");
		checkOutput(params);
	}
	
	public void testMultipleField() throws IOException
	{
		Map<String,String> params = new HashMap<>();
		params.put("TestXY", "one two three");
		params.put("TestAB", "one two three four");
		checkOutput(params);
	}
	
	public void testFileField() throws IOException
	{
		Map<String,String> params = new HashMap<>();
		params.put("TestXY", "one two three");
		FileFieldInfo info = new FileFieldInfo();
		info.fileName = "failname";
		info.name = "naim";
		info.subWriter = new PlainTextWriter("my daita");
		checkOutput(params, Arrays.asList(info));
	}
	
	public void testMultipleFileField() throws IOException
	{
		Map<String,String> params = new HashMap<>();
		params.put("TestXY", "one two three");
		FileFieldInfo info = new FileFieldInfo();
		info.fileName = "failname";
		info.name = "naim";
		info.subWriter = new PlainTextWriter("my daita");
		FileFieldInfo info2 = new FileFieldInfo();
		info2.fileName = "failname2";
		info2.name = "naim2";
		info2.subWriter = new PlainTextWriter("my daita2");
		checkOutput(params, Arrays.asList(info, info2));
	}
	
	private void checkOutput(final Map<String, String> params) throws IOException
	{
		checkOutput(params, Collections.<FileFieldInfo> emptyList());
	}
	
	private void checkOutput(
			final Map<String, String> params, 
			final Collection<FileFieldInfo> fileParams) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FormDataWriter writer = new FormDataWriter()
		{
			@Override
			protected void write() throws IOException
			{
				for(Map.Entry<String,String> entry : params.entrySet())
				{
					addField(entry.getKey(), entry.getValue());
				}
				for(FileFieldInfo info : fileParams)
				{
					addFileField(info.name, info.fileName, info.subWriter);
				}
			}
		};
		writer.write(out);
		
		String expected = createExpectedOutput(writer.boundary, params, fileParams);
		assertEquals(expected, TestUtils.asString(out));
	}
		
	private String createExpectedOutput(String boundary, Map<String, String> params,
			Collection<FileFieldInfo> fileParams) throws IOException
	{
		// lets reimplement the whole FormDataWriter ;-)
		
		StringBuilder expected = new StringBuilder();
		for(Map.Entry<String,String> entry : params.entrySet())
		{
			expected.append(
					"--" + boundary + "\r\n" + 
					"Content-Disposition: form-data; name=\""+entry.getKey()+"\"\r\n" +
					"Content-Type: text/plain; charset=utf-8\r\n\r\n" +
					entry.getValue()+"\r\n");
		}
		for(FileFieldInfo info : fileParams)
		{
			expected.append(
					"--" + boundary + "\r\n" +
					"Content-Disposition: form-data; name=\""+info.name+"\"; " +
							"filename=\"" + info.fileName + "\"\r\n" +
					"Content-Type: "+info.subWriter.getContentType()+"\r\n\r\n");
			ByteArrayOutputStream subOut = new ByteArrayOutputStream();
			info.subWriter.write(subOut);
			expected.append(TestUtils.asString(subOut));
			expected.append("\r\n");
		}
		
		expected.append("--" + boundary + "--\r\n");
		return expected.toString();
	}
	
	private class FileFieldInfo
	{
		String name;
		String fileName;
		ApiRequestWriter subWriter;
	}
}
