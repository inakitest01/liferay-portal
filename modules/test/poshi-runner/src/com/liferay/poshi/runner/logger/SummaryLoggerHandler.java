/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.logger;

import com.liferay.poshi.runner.PoshiRunnerContext;
import com.liferay.poshi.runner.PoshiRunnerException;
import com.liferay.poshi.runner.PoshiRunnerStackTraceUtil;
import com.liferay.poshi.runner.PoshiRunnerVariablesUtil;
import com.liferay.poshi.runner.util.StringUtil;
import com.liferay.poshi.runner.util.Validator;

import org.dom4j.Element;

/**
 * @author Michael Hashimoto
 */
public final class SummaryLoggerHandler {

	public static void failSummary(Element element) {
		failSummary(element, null);
	}

	public static void failSummary(Element element, String message) {
		if (!_isCurrentMajorStep(element)) {
			return;
		}

		LoggerElement statusLoggerElement = new LoggerElement();

		statusLoggerElement.setName("span");
		statusLoggerElement.setText(" --> FAILED");

		_majorStepLoggerElement.addChildLoggerElement(statusLoggerElement);

		LoggerElement errorLoggerElement = new LoggerElement();

		String stackTrace = PoshiRunnerStackTraceUtil.getStackTrace(message);

		stackTrace = StringUtil.replace(stackTrace, "\n", "<br />");
		stackTrace = StringUtil.replace(stackTrace, "\"", "&quot;");

		stackTrace += "<br /><br />";

		errorLoggerElement.setText(stackTrace);

		_majorStepsLoggerElement.addChildLoggerElement(errorLoggerElement);

		_stopLogging();
	}

	public static void passSummary(Element element) {
		if (!_isCurrentMajorStep(element)) {
			return;
		}

		LoggerElement statusLoggerElement = new LoggerElement();

		statusLoggerElement.setName("span");
		statusLoggerElement.setText(" --> PASSED");

		_majorStepLoggerElement.addChildLoggerElement(statusLoggerElement);

		_stopLogging();
	}

	public static void startSummary(Element element) throws Exception {
		if (!_isMajorStep(element)) {
			return;
		}

		_startLogging(element);

		_majorStepLoggerElement = new LoggerElement();

		_majorStepLoggerElement.setText(_getSummary(element));

		_majorStepsLoggerElement.addChildLoggerElement(_majorStepLoggerElement);
	}

	private static String _getSummary(Element element)
		throws PoshiRunnerException {

		String summary = null;

		if (element.attributeValue("summary") != null) {
			summary = element.attributeValue("summary");
		}

		if (summary == null) {
			if (element.attributeValue("action") != null) {
				summary = PoshiRunnerContext.getActionCommandSummary(
					element.attributeValue("action"));
			}
			else if (element.attributeValue("action-summary") != null) {
				summary = PoshiRunnerContext.getActionCommandSummary(
					element.attributeValue("action-summary"));
			}
			else if (element.attributeValue("function") != null) {
				summary = PoshiRunnerContext.getFunctionCommandSummary(
					element.attributeValue("function"));
			}
			else if (element.attributeValue("function-summary") != null) {
				summary = PoshiRunnerContext.getFunctionCommandSummary(
					element.attributeValue("function-summary"));
			}
			else if (element.attributeValue("macro") != null) {
				summary = PoshiRunnerContext.getMacroCommandSummary(
					element.attributeValue("macro"));
			}
			else if (element.attributeValue("macro-summary") != null) {
				summary = PoshiRunnerContext.getMacroCommandSummary(
					element.attributeValue("macro-summary"));
			}
		}

		if (summary != null) {
			return PoshiRunnerVariablesUtil.replaceCommandVars(summary);
		}

		return null;
	}

	private static boolean _isCurrentMajorStep(Element element) {
		if (element == _majorStepElement) {
			return true;
		}

		return false;
	}

	private static boolean _isMajorStep(Element element) throws Exception {
		String summary = _getSummary(element);

		if (summary == null) {
			return false;
		}

		if (!Validator.equals(element.getName(), "execute") &&
			!Validator.equals(element.getName(), "task")) {

			return false;
		}

		if (Validator.isNull(element.attributeValue("function")) &&
			Validator.isNull(element.attributeValue("function-summary")) &&
			Validator.isNull(element.attributeValue("macro")) &&
			Validator.isNull(element.attributeValue("macro-summary")) &&
			Validator.isNull(element.attributeValue("summary"))) {

			return false;
		}

		if (_majorStepElement != null) {
			return false;
		}

		return true;
	}

	private static void _startLogging(Element element) {
		_majorStepElement = element;
	}

	private static void _stopLogging() {
		_majorStepElement = null;
	}

	private static Element _majorStepElement = null;
	private static LoggerElement _majorStepLoggerElement = null;
	private static final LoggerElement _majorStepsLoggerElement =
		new LoggerElement("major-steps");

}