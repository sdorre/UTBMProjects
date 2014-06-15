<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>AP config</title>
	</head>

	<body>
		<form method="post" action="APconfig">
			<fieldset>
				<legend>configuration of Access Points</legend>
				
				<label for="name">Name :</label>
				<input type="text" id="name" name="name" value=""/>
				
				<label for="mac_addr">MAC Address :</label>
				<input type="text" id="mac_addr" name="mac_addr" value="xx:xx:xx:xx:xx:xx" size="17" maxlength="17"/>
				<br/>
				<label for="ip_addr">IP Address :</label>
				<input type="text" id="ip_addr" name="ip_addr" value="X.X.X.X" size="15" maxlength="15" />
				
				<label for="map_id">Maps ID :</label>
				 <select name="map_id">
					<c:forEach items="${ map_list }" var="map" varStatus="boucle">
                    	<option value="${ map.key }">${ map.value }</option>
					</c:forEach>
 				<br/>
				<input type="submit" value="Save" />
			</fieldset>
			<br />
		</form>
		
		<table border="1">
			<th>Name</th>
			<th>MAC Address</th>
			<th>IP Address</th>
			<th>MAP Id</th>
			<th>  </th>
			
			<c:forEach items="${ router }" var="router" varStatus="boucle">
				<tr>
					<td>${ router.name }</td>
					<td>${ router.mac_addr }</td>
					<td>${ router.ip_addr }</td>
					<c:forEach items="${ map_list }" var="map" varStatus="boucle">
                    	<c:if test="${ map.key == router.map_id}"><td>${ map.value }</td></c:if>
					</c:forEach>
					<td><a href="${url}?id=${ router.id }"><button>Delete</button></a></td>	
				</tr>
			</c:forEach>
		</table>
	</body>
</html>