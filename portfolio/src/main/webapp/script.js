// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * fetchs /data information async aka Comments
 */
async function getComments() {
  //passes in user input on number of comments to display
  var x = document.getElementById("maxcom");
  var text = "/data?max-comments=";
  if (x.elements[0].value == ''){
      text += "3"
  }
  else{
      text += x.elements[0].value;
  }
  const response = await fetch(text);
  const data = await response.json();
  const commentEl = document.getElementById('Comments');
  //clear current comment section
  commentEl.innerText = "";
  data.forEach((line)=> {
      commentEl.appendChild(createListElement(line));
      });

}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement
  }

/** Calls delete-data servlet to delete commens and then fetches /data */
async function deleteComments(){
  fetch('/delete-data', {method: 'POST'});
  const response = await fetch("/data");
  const data = await response.json();
  const commentEl = document.getElementById('Comments');
  //clear current comment section
  commentEl.innerText = "";
  data.forEach((line)=> {
      commentEl.appendChild(createListElement(line));
      });
}

/** Creates a map and adds it to the page. */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 37.422, lng: -122.084}, zoom: 16});
}
