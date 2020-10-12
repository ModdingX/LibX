#!/usr/bin/env groovy

pipeline {
    agent any
    stages {
        stage('Clean') {
            steps {
                echo 'Cleaning Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build') {
            steps {
                echo 'Building'
                sh './gradlew build'
            }
        }

        // Publish is left out due to tests

        //stage('Archive artifacts') {
        //    steps {
        //        echo 'Archive'
        //        archiveArtifacts 'build/libs*/*jar'
        //    }
        //}


        /*stage('Publish artifacts') {
            steps {
                echo 'Publishing'
                sh './gradlew publish'
            }
        }*/

        stage('Javadoc') {
            steps {
                echo 'Documentation'
                sh './gradlew javadoc'
                sh 'git checkout gh-pages'
                sh 'copy -rp build/docs/* .'
                sh 'git commit -a'
                sh 'git push'
            }
        }
    }
}