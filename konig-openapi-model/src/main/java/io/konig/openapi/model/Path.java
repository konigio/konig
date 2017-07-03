package io.konig.openapi.model;

public class Path {

	private String stringValue;
	private String summary;
	private String description;
	private String ref;
	private Operation get;
	private Operation put;
	private Operation post;
	private Operation delete;
	private Operation options;
	private Operation head;
	private Operation patch;
	private Operation trace;
	private ParameterList parameterList;
	
	public Path(String stringValue) {
		this.stringValue = stringValue;
	}
	public String stringValue() {
		return stringValue;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public Operation getGet() {
		return get;
	}
	public void setGet(Operation get) {
		this.get = get;
	}
	public Operation getPut() {
		return put;
	}
	public void setPut(Operation put) {
		this.put = put;
	}
	public Operation getPost() {
		return post;
	}
	public void setPost(Operation post) {
		this.post = post;
	}
	public Operation getDelete() {
		return delete;
	}
	public void setDelete(Operation delete) {
		this.delete = delete;
	}
	public Operation getOptions() {
		return options;
	}
	public void setOptions(Operation options) {
		this.options = options;
	}
	public Operation getHead() {
		return head;
	}
	public void setHead(Operation head) {
		this.head = head;
	}
	public Operation getPatch() {
		return patch;
	}
	public void setPatch(Operation patch) {
		this.patch = patch;
	}
	public Operation getTrace() {
		return trace;
	}
	public void setTrace(Operation trace) {
		this.trace = trace;
	}
	public void addParameter(Parameter p) {
		if (parameterList == null) {
			parameterList = new ParameterList();
		}
		parameterList.add(p);
	}
	public ParameterList getParameters() {
		return parameterList;
	}
	public void setParameters(ParameterList parameterList) {
		this.parameterList = parameterList;
	}
}
