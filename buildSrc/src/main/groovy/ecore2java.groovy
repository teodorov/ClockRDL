package org.ensta

import org.eclipse.emf.mwe.utils.StandaloneSetup
import org.eclipse.emf.mwe2.ecore.EcoreGenerator

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class Ecore2JavaGenerationTask extends DefaultTask {
	String description 	= 'Generate Java source code from ECORE model.'
	String modelsUri  	= "platform:/resource/${project.name}/model"
	String srcPath 		= "generated"

	@TaskAction
	def generateJava() {
		//generate project file
		def projectFile = project.file(".project")
			projectFile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			projectFile << "<projectDescription>"
			projectFile << "<name>" + project.name + "</name>"
			projectFile << "</projectDescription>"
		//setup the standalone platform uri
		new StandaloneSetup().with {
			scanClassPath = true
			platformUri = "./${project.name}"
		}
		//get all .genmodel files
		def genmodelNames = project.file("model")
			.listFiles()
			.collect{it.name}
			.findAll{it.endsWith(".genmodel")
		}
		//for each genmodel generate the associated model
		for(genmodel in genmodelNames) {
			println "${modelsUri}/${genmodel}"
			new EcoreGenerator().with {
				genModel = "${modelsUri}/${genmodel}"
				addSrcPath(srcPath)
				generateEdit = false
				generateEditor = false
				invoke(null)
			}
		}
		//delete plugin.*, .project and meta-inf files
		project.delete "plugin.xml", "plugin.properties", "META-INF", ".project"
		//generate build.properties indicating that we use custom build
		project.file("build.properties").write("custom = true\n")
	}
}