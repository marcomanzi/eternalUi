const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<custom-style> 
  <style>
        .Label.h1 {
            font-size: 2em !important;
        }
        
        .Label.h2 {
            font-size: 1.8em !important;
        }
        
        .Label.h3 {
            font-size: 1.6em !important;
        }
        
        .Label.sectionTitle {
            font-size: 1.4em !important;
            font-weight: bold;
        }
        .exampleDialogCssClass {
            width: 90em !important;
        }
    </style> 
 </custom-style>`;

document.head.appendChild($_documentContainer.content);

