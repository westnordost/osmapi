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
				capabilities.setMinSupportedApiVersion(getFloatAttribute("minimum"));
				capabilities.setMaxSupportedApiVersion(getFloatAttribute("maximum"));
				break;
			case "area":
				capabilities.setMaxMapQueryArea(getFloatAttribute("maximum"));
				break;
			case "tracepoints":
				capabilities.setMaxPointsInGpsTracePerPage(getIntAttribute("per_page"));
				break;
			case "waynodes":
				capabilities.setMaxNodesInWay(getIntAttribute("maximum"));
				break;
			case "changesets":
				capabilities.setMaxElementsPerChangeset(getIntAttribute("maximum_elements"));
				break;
			case "timeout":
				capabilities.setTimeoutInSeconds(getIntAttribute("seconds"));
				break;
			case "status":
				capabilities.setDatabaseStatus(Capabilities.parseApiStatus(getAttribute("database")));
				capabilities.setMapDataStatus(Capabilities.parseApiStatus(getAttribute("api")));
				capabilities.setGpsTracesStatus(Capabilities.parseApiStatus(getAttribute("gpx")));
				break;
		}
	}

	@Override
	protected void onEndElement()
	{
		if(POLICY.equals(getParentName()) && IMAGERY.equals(getName()))
		{
			capabilities.setImageryBlacklistRegExes(imageBlacklistRegexes);
			imageBlacklistRegexes = null;
		}
	}
}
