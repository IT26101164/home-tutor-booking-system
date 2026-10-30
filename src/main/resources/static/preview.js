"use strict";
// Day 1 UI fixture only: no authentication and no persistent writes.
window.previewRole = "GUEST";
const previewTutor = {id:1,userId:2,name:"Preview Tutor",subject:"Mathematics",qualification:"BSc",location:"Colombo",bio:"Fictional UI preview. Real tutor data is added on Day 2.",hourlyFee:1500,experienceYears:3,rating:0,reviewCount:0};
window.previewApi = async function(url, method="GET") {
    const path=url.split("?")[0];
    if(method!=="GET") throw new Error("Day 1 is a UI preview. This action does not save data; real APIs arrive on Day 2.");
    if(path==="/session") return {user:window.previewRole==="GUEST"?null:{id:window.previewRole==="TUTOR"?2:3,name:"Preview "+window.previewRole,email:"preview@example.test",role:window.previewRole,active:true},csrfToken:"",csrfHeader:"X-CSRF-TOKEN"};
    if(path==="/tutors/mine" || path==="/tutors/1") return previewTutor;
    if(path==="/tutors") return [previewTutor];
    if(path==="/admin/stats") return {users:0,tutors:1,bookings:0,reviews:0};
    return [];
};
document.addEventListener("DOMContentLoaded",()=>{
    const bar=document.createElement("aside");
    bar.setAttribute("class","card");
    bar.innerHTML='<strong>DAY 1: UI PREVIEW ONLY - no data is saved.</strong> <label>Preview role <select id="preview-role"><option>GUEST</option><option>STUDENT</option><option>TUTOR</option><option>ADMIN</option></select></label> <a href="#home">Home</a> · <a href="#register">Register</a> · <a href="#login">Login</a> · <a href="#search">Search</a>';
    document.body.prepend(bar);
    document.querySelector("#preview-role").addEventListener("change",async e=>{window.previewRole=e.target.value;await session();location.hash=window.previewRole==="ADMIN"?"admin":window.previewRole==="TUTOR"?"teach":"home";await render();});
});
