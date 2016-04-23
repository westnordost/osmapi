package de.westnordost.osmapi.capabilities;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.xml.XmlParser;

/** Parses the osm server capabilites and limits (API 0.6). It parses the XML naively, i.e. it
 *  does not care where in the XML the notes nodes are. */
public class CapabilitiesParser extends XmlParser implements ApiResponseReader<Capabilities>
{
	private static final String API = "api";
	private static final String POLICY = "policy";
	private static final String IMAGERY = "imagery";

	private Capabilities capabilities;
	private List<String> imageBlacklistRegexes;

	@Override
	public Capabilities parse(InputStream in)
	{
		capabilities = new Capabilities();
		doParse(in);
		return capabilities;
	}

	@Override
	protected void onStartElement()
	{
		if(API.equals(getParentName()))
		{
			parseApiElement();
		}
		else if(POLICY.equals(getParentName()))
		{
			if(IMAGERY.equals(getName()))
			{
				imageBlacklistRegexes = new ArrayList<>();
			}
		}
		else if(IMAGERY.equals(getParentName()))
		{
			if("blacklist".equals(getName()))
			{
				imageBlacklistRegexes.add(getAttribute("regex"));
			}
		}
	}

	private void parseApiElement()
	{
		String name = getName();
		switch (name)
		{
			case "version":
				capabilities.minSupportedApiVersion = getFloatAttribute("minimum");
				capabilities.maxSupportedApiVersion = getFloatAttribute("maximum");
				break;
			case "area":
				capabilities.maxMapQueryAreaInSquareDegrees = getFloatAttribute("maximum");
				break;
			case "tracepoints":
				capabilities.maxPointsInGpsTracePerPage = getIntAttribute("per_page");
				break;
			case "waynodes":
				capabilities.maxNodesInWay = getIntAttribute("maximum");
				break;
			case "changesets":
				capabilities.maxElementsPerChangeset = getIntAttribute("maximum_elements");
				break;
			case "timeout":
				capabilities.timeoutInSeconds = getIntAttribute("seconds");
				break;
			case "status":
				capabilities.databaseStatus = Capabilities.parseApiStatus(getAttribute("database"));
				capabilities.mapDataStatus = Capabilities.parseApiStatus(getAttribute("api"));
				capabilities.gpsTracesStatus = Capabilities.parseApiStatus(getAttribute("gpx"));
				break;
		}
	}

	@Override
	protected void onEndElement()
	{
		if(POLICY.equals(getParentName()) && IMAGERY.equals(getName()))
		{
			capabilities.imageryBlacklistRegExes = imageBlacklistRegexes;
			imageBlacklistRegexes = null;
		}
	}
}
