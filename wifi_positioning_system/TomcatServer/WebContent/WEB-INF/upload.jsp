<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>File Uploading Form</title>
		</head>
	<body>
		<h3>Map Upload:</h3>
		
		<form action="upload" method="post" enctype="multipart/form-data">
			<label for="name">Name :</label>
				<input type="text" id="name" name="name" value=""/>
			<br />
			<label for="meters_w">Width (in meters) :</label>
				<input type="text" id="meters_w" name="meters_w" value=""/>
			<br />
			<label for="meters_h">Height (in meter) :</label>
				<input type="text" id="meters_h" name="meters_h" value=""/>
			<br />
			
			<input type="file" name="file" size="50" />
			<br />
			
			<input type="submit" value="Upload an Image" />
			<br />
			<br />
			<p>${(result==0)?'':(result==-1)?'Upload failed.':'New map created !' }</p>
		</form>
	</body>
</html>