/*
 * Copyright (c) 2008-2009,
 * 
 * Digital Enterprise Research Institute, National University of Ireland, 
 * Galway, Ireland
 * http://www.deri.org/
 * http://pipes.deri.org/
 *
 * Semantic Web Pipes is distributed under New BSD License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution and 
 *    reference to the source code.
 *  * The name of Digital Enterprise Research Institute, 
 *    National University of Ireland, Galway, Ireland; 
 *    may not be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.deri.pipes.ui;

import org.integratedmodelling.zk.diagram.components.CustomPort;
import org.integratedmodelling.zk.diagram.components.PortTypeManager;
import org.integratedmodelling.zk.diagram.components.Workspace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Textbox;

/**
 * @author robful
 *
 */
public class TextboxPort extends CustomPort{
	final Textbox textbox;
	final String[] originalText = {""};
	/**
	 * @param arg0
	 * @param arg1
	 */
	public TextboxPort(Workspace workspace, Textbox box) {
		this(workspace,box,"none");
	}
	@Override
	public void detach() {
		super.detach();
		onDisconnect();
	}
	/**
	 * @param workspace
	 * @param textbox2
	 * @param string
	 */
	public TextboxPort(Workspace workspace, Textbox textbox, String position) {
		this(((PipeEditor)workspace).getPTManager(), PipePortType.getPType(PipePortType.TEXTIN),textbox,position);
	}
	/**
	 * @param manager
	 * @param type
	 * @param position
	 * @param portType 
	 * @param textbox2
	 */
	public TextboxPort(PortTypeManager manager, PipePortType type,
			Textbox textbox, String position) {
		super(manager, type);
		setPosition(position);
		setPortType("custom");
		this.textbox = textbox;
	}
	Textbox getTextbox(){
		return textbox;
	}
	/**
	 * 
	 */
	public void onDisconnect() {
		if(textbox.isReadonly()){
			textbox.setText(originalText[0]);
			textbox.setReadonly(false);
		}
	}
	/**
	 * 
	 */
	public void onConnect() {
		if(!textbox.isReadonly()){
			originalText[0] = textbox.getValue();
			textbox.setText("Text [wired]");
			textbox.setReadonly(true);
		}
	}

}
