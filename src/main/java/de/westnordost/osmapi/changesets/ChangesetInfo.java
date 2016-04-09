package de.westnordost.osmapi.changesets;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.Bounds;

/** Includes information for a changeset inclusive the changeset discussion but exclusive the
 *  elements that were changed in the changeset.
 */
public interface ChangesetInfo extends Changeset
{
	boolean isOpen();

	Date getDateCreated();

	/** @return the date the changeset was closed. Null if the changeset is still open  */

	Date getDateClosed();

	/** @return the date the changeset was created */
	@Override
	Date getDate();

	/** @return the bounding box that includes all changes of this changeset. May be null if the
	 *          changeset is empty. */
	Bounds getBounds();

	/** @return map of tags associated with this changeset. May be null if there are no tags at
	 *  all. */
	Map<String, String> getTags();

	/** A shortcut to getTags().get("comment")
	 * @return the "commit comment" of the changeset which should include information about what was
	 *		 changed (and why). null if none supplied.
	 * */
	String getChangesetComment();

	/** A shortcut to getTags().get("source").split(";")
	 * @return the source of the data entered. Common values include "bing", "survey", "local
	 *         knowledge", "common knowledge", "extrapolation", "photograph" (and more). null if
	 *         none supplied.
	 * */
	String[] getSources();

	/** A shortcut to getTags().get("created_by")
	 * @return the application with which this changeset has been created. null if none supplied. */
	String getGenerator();

	/** @return a list of comments that were left for this changeset. May be null if there are none */
	List<ChangesetComment> getDiscussion();

	/** @return the number of comments in this changeset */
	int getCommentsCount();
}
