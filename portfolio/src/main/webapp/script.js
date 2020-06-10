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
clo// limitations under the License.


function getComments() {
    console.log('fetching comments from server');
    
    commentCapacity = document.getElementById('comment-capacity');
    if (commentCapacity == null) commentCapacity = 0;
    
    fetch('/data?comment-capacity=5').then(response => response.json()).then((commentsJson) => {

        console.log(commentsJson);
        const commentListElement= document.getElementById('comments');
        commentListElement.innerHtml = '';3

        for (comment of commentsJson) {
            commentListElement.appendChild(createListElement(comment));
        }
    });
}

function createListElement(text) {
    const liElement = document.createElement("LI");
    liElement.innerText = text;
    return liElement;
}