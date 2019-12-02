package de.westnordost.osmapi.changesets;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;

/** Includes information for a changeset inclusive the changeset discussion but exclusive the
 *  elements that were changed in the changeset.
 */
public class ChangesetInfo extends Changeset implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** map of tags associated with this changeset. May be null if there are no tags at all. */
	public Map<String, String> tags;
	
	/** The changeset discussion. May be null if there is none */
	public List<ChangesetNote> discussion;
	/** the number of notes in the changeset discussion of this changeset */
	public int notesCount;
	/** the number of changes in a changeset */
	public int changesCount;
	
	/** the bounding box that includes all changes of this changeset. May be null if the changeset 
	 * is empty. */
	public BoundingBox boundingBox;
	
	public boolean isOpen;
	
	/** the date the changeset was closed. Null if the changeset is still open  */
	public Date dateClosed;
	
	/** the date the changeset was created. Null if the changeset is still open  */
	public Date dateCreated;
	
	/** A shortcut to getTags().get("comment")
	 * @return the "commit comment" of the changeset which should include information about what was
	 *		 changed (and why). null if none supplied.
	 * */
	public String getChangesetComment()
	{
		return tags != null ? tags.get("comment") : null;
	}

	/** A shortcut to getTags().get("source").split(";")
	 * @return the source of the data entered. Common values include "bing", "survey", "local
	 *         knowledge", "common knowledge", "extrapolation", "photograph" (and more). null if
	 *         none supplied.
	 * */
	public String[] getSources()
	{
		String source = tags != null ? tags.get("source") : null;
		return source != null ? source.split("(\\s)?;(\\s)?") : null;
	}

	/** A shortcut to getTags().get("created_by")
	 * @return the application with which this changeset has been created. null if none supplied. */
	public String getGenerator()
	{
		return tags != null ? tags.get("created_by") : null;
	}
}
