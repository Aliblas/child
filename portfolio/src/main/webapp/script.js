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
// limitations under the License.

window.onload = function() {
    getComments();
    printCommentSender();
}



function getComments() {
    document.getElementById("comments").innerHTML = "";

    var commentCapacity = document.getElementById("comment-number").value;
    
    fetch("/data?comment-capacity=" + commentCapacity).then(response => response.json()).then((commentsJson) => {

        const commentSectionElement= document.getElementById("comments");

        for (comment of commentsJson) {
            console.log(comment);
            json = JSON.parse(comment);
            commentSectionElement.appendChild(createcommentElementFromJson(json));
        }
    });
}

function createcommentElementFromJson(json) {
    
    var nameElement = document.createElement("b");
    var nameText = document.createTextNode(json.name + ": ");
    nameElement.appendChild(nameText);
    var commentText = document.createTextNode(json.comment);

    var comment = document.createElement("p");
    comment.appendChild(nameElement);
    comment.appendChild(commentText);
    return comment;
}

function deleteComments() {
    fetch("/delete-data", { method: "post" }).then(response => console.log(response.text()));
    getComments();
}

function printCommentSender() {
    fetch("/user-data").then(userData => userData.json()).then(Json => {
        commentSenderDiv = document.getElementById("comment-sender-div");

        if (Json.userLoggedIn) {
            commentSenderDiv.innerHTML = commentSender(Json.nickname);
            document.getElementById("nickname-space").innerText = Json.nickname;
        } else {
            commentSenderDiv.innerHTML = LoginLink(Json.loginUrl);
        }
        
    });
}

function LoginLink(loginUrl) {
    linkHtml =
    `<p><a class="hotpink" href="${loginUrl}">Login</a> to post a comment.</p>`;
    return linkHtml;
}

function commentSender() {
    senderHtml = 
    `<p>Commenting as <span class="hotpink"><b id="nickname-space"></b></span>. Visit <a class="hotpink" href="/settings.html">Settings</a> to update nickname.</p>
    <form id="comment-box" action="/data" method="POST">
        <textarea name="comment-input" placeholder="Comment on this page"></textarea>
        <input value="Send" type="submit">
    </form>`;
    return senderHtml;
}