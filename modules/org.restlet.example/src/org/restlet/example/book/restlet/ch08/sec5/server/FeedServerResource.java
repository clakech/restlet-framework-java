package org.restlet.example.book.restlet.ch08.sec5.server;

import java.util.ArrayList;
import java.util.List;

import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Text;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Resource corresponding to an account feed associated to tags.
 */
public class FeedServerResource extends ServerResource {

    @Get("atom")
    public Feed toAtom() throws ResourceException {
        Feed result = new Feed();
        result.setTitle(new Text("Homer's feed"));
        Entry entry;

        for (int i = 1; i < 11; i++) {
            entry = new Entry();
            entry.setTitle(new Text("Mail n�" + i));
            entry.setSummary("Doh! This is the content of mail n�" + i);
            result.getEntries().add(entry);
        }

        return result;
    }

    @Get("rss")
    public SyndFeed toRss() throws ResourceException {
        SyndFeed result = new SyndFeedImpl();
        result.setTitle("Homer's feed");
        result.setDescription("Homer's feed");
        result.setLink(getReference().toString());
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        result.setEntries(entries);
        SyndEntry entry;
        SyndContent description;

        for (int i = 1; i < 11; i++) {
            entry = new SyndEntryImpl();
            entry.setTitle("Mail n�" + i);
            description = new SyndContentImpl();
            description.setValue("Doh! This is the content of mail n�" + i);
            entry.setDescription(description);
            entries.add(entry);
        }

        return result;
    }
}
