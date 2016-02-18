# Example Extensions

Here are some example extensions. 
Each one could be in a plugin, or all of them. Or any combination. 

An extension contributs to an ExtensionPoint which is declared either in a page somewhere, or another extension implementation's markup. 
Some extensions have context data - some are just a place to hang any UI element. 


# Example 
ExtensionPoint is used like this: 

```   
   <div key={pipeline.name}>
       <h3>{pipeline.name}</h3>
       <ExtensionPoint name="jenkins.pipeline.pipelineRow" pipeline={pipeline}/>
   </div>

```

This allows plugins to add more elements (react components). Data passed in as available via props. In this case, this is a pipeline listing, so the pipeline object passed in is the pipeline for the current row. 
So, for example, an extension could implement a status viewer, or a log viewer, for that given pipeline.

See MyPipelineRowExtension as an example that uses the above. 
Extensions themselves can declare other extension points. There is an example of this in AlienPageSubMenu.jsx. 

Extensions that contribute to an extension point are currently registered in `register-plugins.jsx` (this will be generated and should not need to be done manually)
