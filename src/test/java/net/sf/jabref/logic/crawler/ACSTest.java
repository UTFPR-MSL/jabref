package net.sf.jabref.logic.crawler;

import net.sf.jabref.model.entry.BibtexEntry;
import net.sf.jabref.external.FullTextFinder;
import net.sf.jabref.logic.crawler.ACS;
import net.sf.jabref.model.entry.BibtexEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class ACSTest {
    ACS finder;
    BibtexEntry entry;

    @Before
    public void setup() {
        finder = new ACS();
        entry = new BibtexEntry();
    }

    @Test(expected = NullPointerException.class)
    public void rejectNullParameter() throws IOException {
        finder.findFullText(null);
    }

    @Test
    public void findByDOI() throws IOException {
        entry.setField("doi", "10.1021/bk-2006-STYG.ch014");

        Assert.assertEquals(
                Optional.of(new URL("http://pubs.acs.org/doi/pdf/10.1021/bk-2006-STYG.ch014")),
                finder.findFullText(entry)
        );
    }

    @Test
    public void notFoundByDOI() throws IOException {
        entry.setField("doi", "10.1021/bk-2006-WWW.ch014");

        Assert.assertEquals(Optional.empty(), finder.findFullText(entry));
    }
}
