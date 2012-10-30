var url = "hello";
var n=parseInt(
		(url.indexOf('n=')!=-1)?
				url.substring(
					url.indexOf('n=')+2,
					((url.substring(url.indexOf('n=')+2,url.length)).indexOf('&')!=-1)?
							url.indexOf('n=')+2+(url.substring(url.indexOf('n=')+2,url.length)).indexOf('&')
							:
							url.length)
				:
				512);