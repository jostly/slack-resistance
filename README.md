# Slack Resistance

Build: `sbt compile stage` to build and create start script

Deploy to Heroku: `sbt deployHeroku` or combined: `sbt compile stage deployHeroku` 

The Heroku app needs to have the env parameter `HOST` set to 0.0.0.0 to bind properly.
Defaults to localhost to avoid security warnings when running locally.

You also need to configure the env parameter `SLACK_WEBHOOKS` to the Slack incoming Webhooks path,
ie `https://hooks.slack.com/services/...`

