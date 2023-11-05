
var imageDiv = document.getElementsByClassName('pXCGrf')[0];
var originalImage = document.getElementsByTagName('img')[0];
var url = originalImage.src;
var img = new Image;
img.onload = function() {
imageDiv.style.width = img.width + 'px'; imageDiv.style.height = img.height + 'px';
};
img.src = url;

var body = document.getElementsByTagName('body')[0];
body.style['min-width'] ='5000px'; body.style['min-height'] = '5000px';
var div = document.getElementsByClassName('aNrIN')[0];
div.style['width'] ='5000px'; div.style['height'] = '5000px';

var headerPanel = document.getElementsByClassName('pGxpHc')[0];
headerPanel.remove();

var textActionButton = document.getElementById('text').getElementsByTagName('button')[0];
textActionButton.click();

var translateActionButton = document.getElementById('translate').getElementsByTagName('button')[0];
translateActionButton.click();

var btn = document.evaluate("//*[@id='target']//button", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE).singleNodeValue;
btn.click();

var li_1 = document.evaluate("//li", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE).singleNodeValue;
li_1.remove();

var es = document.evaluate("//li[@data-locale='es']", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE).singleNodeValue;
es.click();

var translatePanel = document.getElementsByClassName('z3qvzf')[0];
translatePanel.remove();

var actionPanel = document.getElementsByClassName('SAvApe')[0];
actionPanel.remove();

var wordsPanel = document.getElementsByClassName('b57KQc')[0];
wordsPanel.remove();

var translatedImageDiv = document.evaluate("//*[contains(@class,'pXCGrf')]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE).singleNodeValue;
translatedImageDiv.click();

