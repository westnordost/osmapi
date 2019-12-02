package de.westnordost.osmapi.common.errors;

import java.io.IOException;

/** Thrown when the ISP / Wifi router redirected us to someplace else (i.e. some login page)*/
public class RedirectedException extends IOException
{
	private static final long serialVersionUID = 1L;
}
