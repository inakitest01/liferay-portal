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

package com.liferay.portlet.documentlibrary.asset;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.BaseDDMFieldReader;
import com.liferay.portlet.documentlibrary.model.DLFileEntryMetadata;
import com.liferay.portlet.documentlibrary.service.DLFileEntryMetadataLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMForm;
import com.liferay.portlet.dynamicdatamapping.model.DDMFormField;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormFieldValue;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormValues;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngineUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Adolfo Pérez
 */
public class DLFileEntryDDMFieldReader extends BaseDDMFieldReader {

	public DLFileEntryDDMFieldReader(
		FileEntry dlFileEntry, FileVersion fileVersion) {

		_fileEntry = dlFileEntry;
		_fileVersion = fileVersion;
	}

	@Override
	public DDMFormValues getDDMFormValues() throws PortalException {
		DDMFormValues ddmFormValues = new DDMFormValues(null);

		for (DLFileEntryMetadata dlFileEntryMetadata :
				getDLFileEntryMetadatas()) {

			DDMFormValues ddmStorageDDMFormValues =
				StorageEngineUtil.getDDMFormValues(
					dlFileEntryMetadata.getDDMStorageId());

			for (DDMFormFieldValue ddmFormFieldValue :
					ddmStorageDDMFormValues.getDDMFormFieldValues()) {

				ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
			}
		}

		return ddmFormValues;
	}

	@Override
	public DDMFormValues getDDMFormValues(String ddmType)
		throws PortalException {

		DDMFormValues ddmFormValues = new DDMFormValues(null);

		for (DLFileEntryMetadata dlFileEntryMetadata :
				getDLFileEntryMetadatas()) {

			DDMStructure ddmStructure = dlFileEntryMetadata.getDDMStructure();

			DDMForm ddmForm = ddmStructure.getDDMForm();

			Map<String, DDMFormField> currentDDMFormFieldsMap =
				ddmForm.getDDMFormFieldsMap(false);

			DDMFormValues ddmStorageDDMFormValues =
				StorageEngineUtil.getDDMFormValues(
					dlFileEntryMetadata.getDDMStorageId());

			for (DDMFormFieldValue ddmFormFieldValue :
					ddmStorageDDMFormValues.getDDMFormFieldValues()) {

				DDMFormField ddmFormField = currentDDMFormFieldsMap.get(
					ddmFormFieldValue.getName());

				if (ddmType.equals(ddmFormField.getDataType())) {
					ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
				}
			}
		}

		return ddmFormValues;
	}

	protected List<DLFileEntryMetadata> getDLFileEntryMetadatas() {
		List<DLFileEntryMetadata> dlFileEntryMetadatas =
			new ArrayList<DLFileEntryMetadata>();

		List<DDMStructure> ddmStructures =
			DDMStructureLocalServiceUtil.getClassStructures(
				_fileEntry.getCompanyId(),
				PortalUtil.getClassNameId(DLFileEntryMetadata.class));

		for (DDMStructure ddmStructure : ddmStructures) {
			DLFileEntryMetadata dlFileEntryMetadata =
				DLFileEntryMetadataLocalServiceUtil.fetchFileEntryMetadata(
					ddmStructure.getStructureId(),
					_fileVersion.getFileVersionId());

			if (dlFileEntryMetadata == null) {
				continue;
			}

			dlFileEntryMetadatas.add(dlFileEntryMetadata);
		}

		return dlFileEntryMetadatas;
	}

	private final FileEntry _fileEntry;
	private final FileVersion _fileVersion;

}