package com.vcareinc.services;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.vcareinc.constants.OptionType;
import com.vcareinc.constants.UploadFileType;
import com.vcareinc.exceptions.ValidationException;
import com.vcareinc.models.BaseModel;
import com.vcareinc.services.amazonws.AmazonS3Service;
import com.vcareinc.vo.FileUpload;

public class BaseService<T> {

	@Autowired
	private Validator validator;

	protected EntityManager em;

	@Autowired
	private AmazonS3Service amazonS3Service;

	@PersistenceContext
	protected void setEm(EntityManager em) {
		this.em = em;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void clearError(BaseModel model) {
		model.setErrorCode(null);
		model.setErrorMsg(null);
		model.setErrorConstraintViolation(null);
	}

	protected void clearObject(BaseModel<T> model) {
		model = new BaseModel<T>();
	}

	public void validate(BaseModel<T> model) throws ValidationException {
		Set<ConstraintViolation<BaseModel<T>>> constraint = validator.validate(model);
		if(constraint.size() > 0) {
			model.setErrorConstraintViolation(constraint);
			throw new ValidationException("Validate get occur!!!");
		}
	}

	protected String getFileName(Long id, OptionType optionType, MultipartFile file) {
		String filename = null;
		String originalFilename = file.getOriginalFilename();
		filename = id + "_" + optionType.name() +  "_" + file.getName() + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
		return filename;
	}

	protected FileUpload saveFileUpload(Long id, String filename, MultipartFile file) {
		FileUpload fileUpload = new FileUpload();
		amazonS3Service.uploadFileByMultipartFile(file, filename);

		fileUpload.setClientFilename(file.getOriginalFilename());
		fileUpload.setFilename(filename);
		fileUpload.setUploadType(UploadFileType.AMAZON_S3);

		return fileUpload;
	}

	public AmazonS3Service getAmazonS3Service() {
		return amazonS3Service;
	}

	public void setAmazonS3Service(AmazonS3Service amazonS3Service) {
		this.amazonS3Service = amazonS3Service;
	}
}
