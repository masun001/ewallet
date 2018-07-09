package com.morningtech.eth.server.controller;

import org.apache.commons.lang3.StringEscapeUtils;

import java.beans.PropertyEditorSupport;

/**
* @Description: 字符串防注入攻击
* @author xuchunlin
* @date 2017/11/28 19:14
* @version V1.0
*/
public class StringEscapeEditor extends PropertyEditorSupport {

	private boolean escapeHTML;

	private boolean escapeJavaScript;

	private boolean escapeSQL;
	
	public StringEscapeEditor() {
		super();
	}

	/**
	 *  防止XSS、SQL注入攻击
	 * @param escapeHTML
	 * @param escapeJavaScript
	 * @param escapeSQL
	 */
	public StringEscapeEditor(boolean escapeHTML, boolean escapeJavaScript,
                              boolean escapeSQL) {

		super();

		this.escapeHTML = escapeHTML;

		this.escapeJavaScript = escapeJavaScript;

		this.escapeSQL = escapeSQL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) {
		if (text == null) {
			setValue(null);
		} else {
			String value = text;
			if (escapeHTML) {
				value = StringEscapeUtils.escapeHtml3(value);
			}
			if (escapeJavaScript) {
				value = StringEscapeUtils.escapeEcmaScript(value);
			}
			if (escapeSQL) {
				value = StringEscapeUtils.escapeJava(value);
			}
			setValue(value);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		Object value = getValue();
		return value != null ? value.toString() : "";
	}
	
}
