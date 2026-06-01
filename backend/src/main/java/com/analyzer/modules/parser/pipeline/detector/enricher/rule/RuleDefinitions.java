package com.analyzer.modules.parser.pipeline.detector.enricher.rule;

import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import static com.analyzer.modules.parser.pipeline.detector.enricher.rule.ExtractionRule.*;

/**
 * 规则配置
 */
@Configuration
public class RuleDefinitions {
    private static final List<CodeChunkType> JAVA_TYPES = List.of(
            CodeChunkType.CLASS, CodeChunkType.INTERFACE, CodeChunkType.ENUM,
            CodeChunkType.CONTROLLER, CodeChunkType.SERVICE, CodeChunkType.REPOSITORY,
            CodeChunkType.ENTITY, CodeChunkType.CONFIGURATION, CodeChunkType.COMPONENT
    );

    /**
     * 返回包含解析 java method template... 规则的 Registry
     * @return 规则
     */
    @Bean
    public RuleRegistry ruleRegistry() {
        RuleRegistry registry = new RuleRegistry();
        registerJavaKeywords(registry);
        registerMethodKeywords(registry);
        registerTemplateKeywords(registry);
        registerScriptKeywords(registry);
        registerStyleKeywords(registry);
        registerConfigKeywords(registry);
        registerMetadataRules(registry);
        return registry;
    }
    private void registerJavaKeywords(RuleRegistry registry) {
        List<ExtractionRule> rules = List.of(
                contentContains("GetMapping", "http:GET"),
                contentContains("PostMapping", "http:POST"),
                contentContains("PutMapping", "http:PUT"),
                contentContains("DeleteMapping", "http:DELETE"),
                contentContains("RequestMapping", "http-endpoint"),
                contentContains("@Transactional", "transactional"),
                contentContains("@Autowired", "dependency-injection"),
                contentContains("@Inject", "dependency-injection"),
                contentContains("JpaRepository", "spring-data"),
                contentContains("CrudRepository", "spring-data"),
                contentContains("@Entity", "jpa-entity"),
                contentContains("implements ", "has-interface-impl"),
                contentContains("extends ", "has-inheritance"),
                contentContains("abstract ", "abstract"),
                contentContains("@Scheduled", "scheduled-task"),
                contentContains("@Async", "async"),
                contentContains("@Cacheable", "cached"),
                contentContains("@CacheEvict", "cached")
        );
        registry.addKeywordRules(JAVA_TYPES, rules);
    }
    private void registerMethodKeywords(RuleRegistry registry) {
        registry.addKeywordRules(CodeChunkType.METHOD, List.of(
                contentContains("GetMapping", "http:GET"),
                contentContains("PostMapping", "http:POST"),
                contentContains("PutMapping", "http:PUT"),
                contentContains("DeleteMapping", "http:DELETE"),
                contentContains("throw ", "throws-exception"),
                contentContains("try", "has-try-catch"),
                contentContains("synchronized", "synchronized"),
                contentContains("@Transactional", "transactional"),
                contentContains("return ", "has-return"),
                contentContains("async", "async"),
                contentContains("await", "await"),
                contentContains("Promise", "promise"),
                contentContains("fetch(", "http-client"),
                contentContains("axios", "http-client")
        ));
    }
    private void registerTemplateKeywords(RuleRegistry registry) {
        registry.addKeywordRules(CodeChunkType.TEMPLATE, List.of(
                contentContains("th:", "thymeleaf"),
                contentContains("v-if", "vue-directive"),
                contentContains("v-for", "vue-directive"),
                contentContains("v-bind", "vue-directive"),
                contentContains("ng-", "angular"),
                contentContains("*ngIf", "angular"),
                contentContains("{{", "template-interpolation"),
                contentContains("<form", "form"),
                contentContains("<table", "table"),
                contentContains("<a ", "navigation"),
                contentContains("href", "navigation")
        ));
    }
    private void registerScriptKeywords(RuleRegistry registry) {
        registry.addKeywordRules(CodeChunkType.SCRIPT, List.of(
                contentContains("useState", "react-hooks"),
                contentContains("useEffect", "react-hooks"),
                contentContains("defineComponent", "vue-composition"),
                contentContains("ref(", "vue-composition"),
                contentContains("@Injectable", "angular-di"),
                contentContains("fetch(", "http-client"),
                contentContains("axios", "http-client"),
                contentContains("addEventListener", "event-listener"),
                contentContains("export default", "default-export"),
                contentContains("async", "async"),
                contentContains("await", "await")
        ));
    }
    private void registerStyleKeywords(RuleRegistry registry) {
        registry.addKeywordRules(CodeChunkType.STYLE, List.of(
                contentContains("@media", "responsive"),
                contentContains("flex", "layout"),
                contentContains("grid", "layout"),
                contentContains("animation", "animation"),
                contentContains("transition", "animation"),
                contentContains(":root", "css-variables"),
                contentContains("var(--", "css-variables")
        ));
    }
    private void registerConfigKeywords(RuleRegistry registry) {
        registry.addKeywordRules(CodeChunkType.CONFIG_FILE, List.of(
                contentContains("datasource", "database-config"),
                contentContains("redis", "redis-config"),
                contentContains("server.port", "server-config"),
                fileNameContains("application", "spring-config"),
                fileNameContains("pom", "maven"),
                fileNameContains("gradle", "gradle"),
                fileNameContains("docker", "docker"),
                fileNameContains("package.json", "npm")
        ));
    }
    private void registerMetadataRules(RuleRegistry registry) {
        // 模板引擎
        registry.addMetadataRule(CodeChunkType.TEMPLATE, when(c -> c.getContent().contains("th:"), "templateEngine:thymeleaf"));
        registry.addMetadataRule(CodeChunkType.TEMPLATE, when(c -> c.getContent().contains("v-if"), "templateEngine:vue"));
        registry.addMetadataRule(CodeChunkType.TEMPLATE, when(c -> c.getContent().contains("{{"), "templateEngine:mustache"));
        registry.addMetadataRule(CodeChunkType.TEMPLATE, when(c -> c.getContent().contains("<%"), "templateEngine:jsp"));
        // 配置格式
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".yml", "configFormat:yaml"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".yaml", "configFormat:yaml"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".properties", "configFormat:properties"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".json", "configFormat:json"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".xml", "configFormat:xml"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, pathEndsWith(".toml", "configFormat:toml"));
        // 配置用途
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, fileNameContains("application", "configPurpose:spring-application"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, fileNameContains("pom", "configPurpose:maven-build"));
        registry.addMetadataRule(CodeChunkType.CONFIG_FILE, fileNameContains("docker", "configPurpose:container"));
        // 样式预处理器
        registry.addMetadataRule(CodeChunkType.STYLE, pathEndsWith(".scss", "preprocessor:scss"));
        registry.addMetadataRule(CodeChunkType.STYLE, pathEndsWith(".less", "preprocessor:less"));
        registry.addMetadataRule(CodeChunkType.STYLE, pathEndsWith(".sass", "preprocessor:sass"));
        registry.addMetadataRule(CodeChunkType.STYLE, pathEndsWith(".css", "preprocessor:css"));
        // 脚本框架
        registry.addMetadataRule(CodeChunkType.SCRIPT, when(c -> c.getContent().contains("useState"), "framework:react"));
        registry.addMetadataRule(CodeChunkType.SCRIPT, when(c -> c.getContent().contains("defineComponent"), "framework:vue"));
        registry.addMetadataRule(CodeChunkType.SCRIPT, when(c -> c.getContent().contains("@NgModule"), "framework:angular"));
        // 脚本模块类型
        registry.addMetadataRule(CodeChunkType.SCRIPT, when(c -> c.getContent().contains("export default"), "moduleType:default-export"));
        registry.addMetadataRule(CodeChunkType.SCRIPT, when(c -> c.getContent().contains("module.exports"), "moduleType:commonjs"));
    }
}
