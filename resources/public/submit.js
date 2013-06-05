function jsError(){

  document.getElementById("user-reset").innerHTML="<b>Unable to process your request, contact your system <a href=\"mailto:itdept@ferraracompany.com\">administrator</a>!</b>";

}

function jsSubmit(user,pass){

  if ((user.value == "") || (pass.value == "")){alert("Please type in your username and a new password!"); return;}

  var xmlhttp;

  if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
    xmlhttp=new XMLHttpRequest();
  }
  else
  {// code for IE6, IE5
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }



  xmlhttp.open("GET","/"+user.value+"/reset?pass="+escape(pass.value),true);

  //Send the proper header information along with the request
  //var params = "pass="+escape(pass.value);
//alert(params);
//  http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
//  http.setRequestHeader("Content-length", params.length);
//  http.setRequestHeader("Connection", "close");

  document.getElementById("user-reset").innerHTML="<b>preparing user password reset request...</b>";




  xmlhttp.onreadystatechange=function()
  {
    if (xmlhttp.readyState==4 && xmlhttp.status==200)
      {
      if (xmlhttp.responseText=="Success"){
        document.getElementById("user-reset").innerHTML="<b>Check your email for the password reset link! </br> You must click this link to finalize the password reset process</b>";
      }
      else{jsError();}
    }
    else if (xmlhttp.readyState==4 && xmlhttp.status!=200)
    {jsError();}

  }



  xmlhttp.send();//params);

}