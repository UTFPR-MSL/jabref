/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.collab;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.model.database.KeyCollisionException;
import net.sf.jabref.logic.id.IdGenerator;
import net.sf.jabref.logic.l10n.Localization;
import net.sf.jabref.model.database.BibtexDatabase;
import net.sf.jabref.model.entry.BibtexString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jabref.gui.undo.NamedCompound;
import net.sf.jabref.gui.undo.UndoableInsertString;
import net.sf.jabref.gui.undo.UndoableStringChange;

class StringChange extends Change {

    private static final long serialVersionUID = 1L;
    private final BibtexString string;
    private final String mem;
    private final String disk;
    private final String label;

    private final InfoPane tp = new InfoPane();
    private final JScrollPane sp = new JScrollPane(tp);
    private final BibtexString tmpString;
    
    private static final Log LOGGER = LogFactory.getLog(StringChange.class);


    public StringChange(BibtexString string, BibtexString tmpString, String label,
            String mem, String tmp, String disk) {
        this.tmpString = tmpString;
        name = Localization.lang("Modified string") + ": '" + label + '\'';
        this.string = string;
        this.label = label;
        this.mem = mem;
        this.disk = disk;

        StringBuilder sb = new StringBuilder();
        sb.append("<HTML><H2>");
        sb.append(Localization.lang("Modified string"));
        sb.append("</H2><H3>");
        sb.append(Localization.lang("Label")).append(":</H3>");
        sb.append(label);
        sb.append("<H3>");
        sb.append(Localization.lang("New content")).append(":</H3>");
        sb.append(disk);
        if (string != null) {
            sb.append("<H3>");
            sb.append(Localization.lang("Current content")).append(":</H3>");
            sb.append(string.getContent());
        } else {
            sb.append("<P><I>");
            sb.append(Localization.lang("Cannot merge this change")).append(": ");
            sb.append(Localization.lang("The string has been removed locally")).append("</I>");
        }
        sb.append("</HTML>");
        tp.setText(sb.toString());
    }

    @Override
    public boolean makeChange(BasePanel panel, BibtexDatabase secondary, NamedCompound undoEdit) {
        if (string != null) {
            string.setContent(disk);
            undoEdit.addEdit(new UndoableStringChange(panel, string, false, mem, disk));
            // Update tmp databse:

        } else {
            // The string was removed or renamed locally. We guess that it was removed.
            String newId = IdGenerator.next();
            BibtexString bs = new BibtexString(newId, label, disk);
            try {
                panel.database().addString(bs);
                undoEdit.addEdit(new UndoableInsertString(panel, panel.database(), bs));
            } catch (KeyCollisionException ex) {
                LOGGER.info("Error: could not add string '" + string.getName() + "': " + ex.getMessage());
            }
        }

        // Update tmp database:
        if (tmpString != null) {
            tmpString.setContent(disk);
        }
        else {
            BibtexString bs = new BibtexString(IdGenerator.next(), label, disk);
            secondary.addString(bs);
        }

        return true;
    }

    @Override
    JComponent description() {
        return sp;
    }

}
