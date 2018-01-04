<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><%= request.getAttribute("javax.servlet.error.status_code") %> Error</title>
<style>
#errorbg {
	background-color: #fcabab;
	border: 1px solid red;
	vertical-align: middle;
	padding: 20px 20px;
	width:50%;
	text-align:center;
	margin:0 auto;
}
</style>
</head>
<body>
<div id="errorbg"> <%= request.getAttribute("javax.servlet.error.message") %> </div>
</body>
</html>