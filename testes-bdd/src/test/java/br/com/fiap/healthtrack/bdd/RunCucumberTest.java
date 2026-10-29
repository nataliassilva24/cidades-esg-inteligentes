package br.com.fiap.healthtrack.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Ponto de entrada da suíte BDD: executa todos os arquivos .feature de src/test/resources/features.
 * Relatórios gerados em target/cucumber-reports (HTML e JSON).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.fiap.healthtrack.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/relatorio.html, json:target/cucumber-reports/relatorio.json")
public class RunCucumberTest {
}
