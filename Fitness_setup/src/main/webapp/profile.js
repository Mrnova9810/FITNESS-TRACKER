document.addEventListener("DOMContentLoaded", () => {

    // Tabs
    const profileTab = document.getElementById("profileTab");
    const editTab = document.getElementById("editTab");

    const profileView = document.getElementById("profileView");
    const editView = document.getElementById("editView");

    // Profile data
    const p_username = document.getElementById("p_username");
    const p_email = document.getElementById("p_email");
    const p_goal = document.getElementById("p_goal");
    const p_age = document.getElementById("p_age");
    const p_height = document.getElementById("p_height");

    // Edit inputs
    const e_username = document.getElementById("e_username");
    const e_email = document.getElementById("e_email");
    const e_password = document.getElementById("e_password");
    const e_goal = document.getElementById("e_goal");
    const e_age = document.getElementById("e_age");
    const e_height = document.getElementById("e_height");

    const saveBtn = document.getElementById("saveBtn");

    // Avatar
    const avatarImg = document.getElementById("avatarImg");
    const fileInput = document.getElementById("fileInput");

    let isEditMode = false;

    // ----------------------
    // TAB SWITCH
    // ----------------------
    profileTab.onclick = () => {
        profileView.style.display = "block";
        editView.style.display = "none";

        profileTab.classList.add("active");
        editTab.classList.remove("active");

        isEditMode = false;
    };

    editTab.onclick = () => {
        profileView.style.display = "none";
        editView.style.display = "block";

        profileTab.classList.remove("active");
        editTab.classList.add("active");

        isEditMode = true;

        // fill inputs
        e_username.value = p_username.innerText;
        e_email.value = p_email.innerText;
        e_goal.value = p_goal.innerText;
        e_age.value = p_age.innerText;
        e_height.value = p_height.innerText;
    };

    // ----------------------
    // LOAD PROFILE
    // ----------------------
    function loadProfile() {
        fetch("getProfile")
        .then(res => res.json())
        .then(user => {
            p_username.innerText = user.username || "";
            p_email.innerText = user.email || "";
            p_goal.innerText = user.goal || "";
            p_age.innerText = user.age || "";
            p_height.innerText = user.height || "";


              avatarImg.src = user.profilePic || "profileImages/default.png";
        });
    }

    // ----------------------
    // SAVE PROFILE
    // ----------------------
saveBtn.onclick = () => {

    fetch("updateProfile", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            username: e_username.value,
            email: e_email.value,
            password: e_password.value,
            goal: e_goal.value,
            age: String(e_age.value),
            height: String(e_height.value)
        })
    })
    .then(() => {
        loadProfile();
        profileTab.click();
    });
};

    // ----------------------
    // AVATAR CLICK
    // ----------------------
    avatarImg.onclick = () => {
        if (isEditMode) {
            fileInput.click();
        }
    };

    // ----------------------
    // IMAGE PREVIEW + UPLOAD
    // ----------------------
    fileInput.onchange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        avatarImg.src = URL.createObjectURL(file);

        let formData = new FormData();
        formData.append("file", file);
        fetch("uploadProfilePic", {
            method: "POST",
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            console.log("Upload success:", data);

            loadProfile();           // refresh UI
            fileInput.disabled = false;
        })
        .catch(err => {
            console.error("Upload failed:", err);
            fileInput.disabled = false;
        });

    };

    // INIT
    loadProfile();
});

// BACK
function goBack() {
    window.location.href = "dashboard.html";
}