const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<custom-style> 
  <style>
        .Label.h1 {
            font-size: 2em !important;
        }
    </style> 
 </custom-style>`;

document.head.appendChild($_documentContainer.content);

