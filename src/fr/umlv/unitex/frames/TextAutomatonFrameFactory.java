/*
 * Unitex
 *
 * Copyright (C) 2001-2010 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */

package fr.umlv.unitex.frames;

public class TextAutomatonFrameFactory {

	private TextAutomatonFrame frame;
	private boolean existsButNotTfstIsLoaded=false;
	
	TextAutomatonFrame newTextAutomatonFrame() {
		if (frame==null) {
			frame=new TextAutomatonFrame();
		} else {
			frame.hideFrame();
		}
		if (!frame.loadTfst()) {
			existsButNotTfstIsLoaded=true;
			return null;
		}
		existsButNotTfstIsLoaded=false;
		frame.loadSentence(1);
		return frame;
	}

	
	void closeTextAutomatonFrame() {
		if (frame==null) return;
		frame.hideFrame();
	}


	TextAutomatonFrame getTextAutomatonFrame() {
		if (frame!=null && existsButNotTfstIsLoaded) {
			/* We don't want to act as if the frame really exists if
			 * the .tfst has not been loaded */
			return null;
		}
		return frame;
	}
}