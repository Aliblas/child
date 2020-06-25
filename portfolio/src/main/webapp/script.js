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

    let commentCapacity = document.getElementById("comment-number").value;
    
    fetch("/data?comment-capacity=" + commentCapacity).then(response => response.json()).then((commentsJson) => {

        const commentSectionElement= document.getElementById("comments");

        for (comment of commentsJson) {
            console.log(comment);
            json = JSON.parse(comment);
            commentSectionElement.appendChild(createCommentElementFromJson(json));
        }
    });
}

function createCommentElementFromJson(json) {
    let nameElement = document.createElement("b");
    let nameText = document.createTextNode(json.name + ": ");
    nameElement.appendChild(nameText);
    let commentText = document.createTextNode(json.comment);

    let comment = document.createElement("p");
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
        commentField = document.getElementById("comment-field");
        loginLinkDiv = document.getElementById("login-link-div");
        if (Json.userLoggedIn) {
            commentField.style.display = "block";
            loginLinkDiv.style.display = "none";
            document.getElementById("nickname-space").innerText = Json.nickname;
        } else {
            commentField.style.display = "none";
            loginLinkDiv.style.display = "block";
            document.getElementById("login-link").href = Json.loginUrl;
        }
    });
}