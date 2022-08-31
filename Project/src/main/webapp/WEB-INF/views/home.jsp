<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<html>
<head>
<meta charset="utf-8" />
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
	})
	
	function GO(){
		var f = document.createElement('form');
		f.setAttribute('method', 'post');
		f.setAttribute('action', '/test/coin/coin');
		document.body.appendChild(f);
		f.submit();
	}
</script>
</head>
<body>
<h1>
	Hello world!  
</h1>
<button onclick="GO();">GO</button>
</body>
</html>