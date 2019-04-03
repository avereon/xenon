# Product Cards

${project.name} used a special file to specify metadata regarding products. The
file is a JSON formatted file with several fields related to the product. The 
file is located in the META-INF folder of the product jar file.

## Sample Product Card

Below is a sample product card with sample data for all fields:

	{
	  "group": "com.example",
	  "artifact": "sample",
	  "version": "1.2",
	  "timestamp": "39847938409237",
	  "iconUri": "https://example.com/images/sample-mod.png",
	  "name": "Sample",
	  "provider": "The Example Organization",
	  "providerUrl": "https://www.example.com",
	  "inception": "2018",
	  "summary": "An example mod for ${project.name}",
	  "description": "This example mod is for reference purposes only",
	  "copyrightSummary": "All rights reserved.",
	  "licenseSummary": "Sample comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions.",
	  "productUri": "https://www.example.com/sample",
	  "mainClass": "com.example.sample.SampleMod",
	  "javaVersion": "11",
	  "maintainers": [
	  {
	    "name": "Jane Doe",
	    "email": "jane.doe@example.com",
	    "timezone": "US/Eastern",
	    "organization": "Example Organization",
	    "organizationUrl": "https://www.example.com",
	    "roles": [ "Architect", "Developer" ]
	  }
	  ],
	  "contributors": []
	}
