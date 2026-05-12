import React, { useState, useEffect } from 'react';
import { Languages, MessageCircle, BookOpen, Sparkles, RefreshCw, CheckCircle2, Lightbulb, Loader2, AlertCircle, X } from 'lucide-react';
import {
  generateScenario,
  getAvailableScenarios,
  getPhrases,
  evaluateExpression,
  getDailyQuote,
  type ScenarioRequest,
  type ScenarioResponse,
  type ScenarioInfo,
  type DailyQuoteDTO,
} from '../api/agileEnglish';

const AgileEnglishPage: React.FC = () => {
  const [scenarios, setScenarios] = useState<ScenarioInfo[]>([]);
  const [phrases, setPhrases] = useState<Record<string, string[]>>({});
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  
  // 表单状态
  const [selectedScenario, setSelectedScenario] = useState<string>('');
  const [role, setRole] = useState<string>('Software Engineer');
  const [industry, setIndustry] = useState<string>('Technology');
  const [difficulty, setDifficulty] = useState<string>('intermediate');
  const [customContext, setCustomContext] = useState<string>('');
  
  // 结果状态
  const [currentScenario, setCurrentScenario] = useState<ScenarioResponse | null>(null);
  const [userExpression, setUserExpression] = useState<string>('');
  const [evaluationResult, setEvaluationResult] = useState<any>(null);
  const [evaluating, setEvaluating] = useState(false);

  // 错误提示状态
  const [error, setError] = useState<string | null>(null);

  // 每日一句状态
  const [dailyQuote, setDailyQuote] = useState<DailyQuoteDTO | null>(null);

  // 加载场景列表和短语
  useEffect(() => {
    loadInitialData();
    loadDailyQuote();
  }, []);

  const loadInitialData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [scenariosRes, phrasesRes] = await Promise.all([
        getAvailableScenarios(),
        getPhrases(),
      ]);
      console.log('加载场景列表:', scenariosRes);
      console.log('加载短语:', phrasesRes);
      setScenarios(scenariosRes);
      setPhrases(phrasesRes);
      
      // 默认选择第一个场景
      if (scenariosRes.length > 0) {
        setSelectedScenario(scenariosRes[0].id);
      }
    } catch (error) {
      console.error('加载数据失败', error);
      setError('加载场景数据失败，请刷新页面重试');
    } finally {
      setLoading(false);
    }
  };

  const loadDailyQuote = async () => {
    try {
      const quote = await getDailyQuote();
      setDailyQuote(quote);
    } catch (error) {
      console.error('获取每日一句失败', error);
    }
  };

  // 生成场景
  const handleGenerateScenario = async () => {
    if (!selectedScenario) {
      setError('请选择一个场景类型');
      setTimeout(() => setError(null), 3000);
      return;
    }

    setGenerating(true);
    setError(null);
    setEvaluationResult(null);
    setUserExpression('');
    
    try {
      const request: ScenarioRequest = {
        scenarioType: selectedScenario,
        role,
        industry,
        difficultyLevel: difficulty,
        customContext: customContext || undefined,
      };

      const response = await generateScenario(request);
      setCurrentScenario(response);
    } catch (error: any) {
      setError(error.message || '生成场景失败');
      console.error(error);
    } finally {
      setGenerating(false);
    }
  };

  // 评估表达
  const handleEvaluate = async () => {
    if (!userExpression.trim()) {
      setError('请输入您的表达');
      setTimeout(() => setError(null), 3000);
      return;
    }

    setEvaluating(true);
    setError(null);
    try {
      const result = await evaluateExpression({
        userExpression,
        scenarioType: selectedScenario,
        context: currentScenario?.dialogue || '',
      });
      setEvaluationResult(result);
    } catch (error: any) {
      setError(error.message || '评估失败');
      console.error(error);
    } finally {
      setEvaluating(false);
    }
  };

  // 重置
  const handleReset = () => {
    setCurrentScenario(null);
    setEvaluationResult(null);
    setUserExpression('');
    setCustomContext('');
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <Loader2 className="w-8 h-8 text-primary-500 animate-spin" />
        <span className="ml-2 text-slate-600 dark:text-slate-400">加载中...</span>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      {/* 错误提示 */}
      {error && (
        <div className="relative bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-4 flex items-start gap-3 animate-in slide-in-from-top-2">
          <AlertCircle className="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm text-red-800 dark:text-red-300 font-medium">{error}</p>
          </div>
          <button
            onClick={() => setError(null)}
            className="text-red-400 hover:text-red-600 dark:text-red-500 dark:hover:text-red-300 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* 每日一句卡片 */}
      {dailyQuote && (
        <div className="bg-gradient-to-r from-blue-50 to-cyan-50 dark:from-blue-900/20 dark:to-cyan-900/20 border-l-4 border-blue-500 p-4 rounded-xl shadow-sm animate-in fade-in slide-in-from-top-4">
          <p className="text-lg font-semibold text-slate-800 dark:text-slate-200 mb-1">{dailyQuote.english}</p>
          <p className="text-sm text-slate-600 dark:text-slate-400">{dailyQuote.chinese}</p>
        </div>
      )}

      {/* 页面标题 */}
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center text-white shadow-lg">
          <Languages className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            敏捷开发英语练习
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            模拟真实工作场景，提升职场英语表达能力
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：场景配置 */}
        <div className="lg:col-span-1">
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <Sparkles className="w-5 h-5 text-blue-500" />
              <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
                场景配置
              </h2>
            </div>

            <div className="space-y-4">
              {/* 场景类型 */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  场景类型
                </label>
                <select
                  value={selectedScenario}
                  onChange={(e) => setSelectedScenario(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                >
                  {scenarios.map((scenario) => (
                    <option key={scenario.id} value={scenario.id}>
                      {scenario.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* 角色 */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  角色
                </label>
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                >
                  <option value="Software Engineer">Software Engineer</option>
                  <option value="Tech Lead">Tech Lead</option>
                  <option value="Product Manager">Product Manager</option>
                  <option value="Scrum Master">Scrum Master</option>
                  <option value="QA Engineer">QA Engineer</option>
                  <option value="DevOps Engineer">DevOps Engineer</option>
                </select>
              </div>

              {/* 行业 */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  行业
                </label>
                <select
                  value={industry}
                  onChange={(e) => setIndustry(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                >
                  <option value="Technology">Technology</option>
                  <option value="Finance">Finance</option>
                  <option value="E-commerce">E-commerce</option>
                  <option value="Healthcare">Healthcare</option>
                  <option value="Education">Education</option>
                </select>
              </div>

              {/* 难度级别 */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  难度级别
                </label>
                <select
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                >
                  <option value="beginner">Beginner (初级)</option>
                  <option value="intermediate">Intermediate (中级)</option>
                  <option value="advanced">Advanced (高级)</option>
                </select>
              </div>

              {/* 自定义上下文 */}
              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  自定义上下文（可选）
                </label>
                <textarea
                  value={customContext}
                  onChange={(e) => setCustomContext(e.target.value)}
                  placeholder="描述具体的项目背景或技术栈..."
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                  rows={3}
                />
              </div>

              {/* 生成按钮 */}
              <button
                onClick={handleGenerateScenario}
                disabled={generating}
                className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white rounded-lg transition-colors font-medium"
              >
                {generating ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    生成中...
                  </>
                ) : (
                  <>
                    <MessageCircle className="w-4 h-4" />
                    生成对话场景
                  </>
                )}
              </button>

              {currentScenario && (
                <button
                  onClick={handleReset}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2.5 border border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-lg transition-colors font-medium"
                >
                  <RefreshCw className="w-4 h-4" />
                  重新生成
                </button>
              )}
            </div>
          </div>
        </div>

        {/* 右侧：对话场景展示 */}
        <div className="lg:col-span-2">
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <MessageCircle className="w-5 h-5 text-green-500" />
                <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
                  对话场景
                </h2>
              </div>
              {currentScenario && (
                <span className="px-3 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-full text-sm font-medium">
                  {currentScenario.scenarioType}
                </span>
              )}
            </div>

            {!currentScenario ? (
              <div className="text-center py-12">
                <MessageCircle className="w-16 h-16 text-slate-300 dark:text-slate-600 mx-auto mb-4" />
                <p className="text-slate-500 dark:text-slate-400">
                  配置场景参数后点击生成
                </p>
              </div>
            ) : (
              <div className="space-y-6">
                {/* 练习提示 */}
                {currentScenario.practiceTips && currentScenario.practiceTips.length > 0 && (
                  <div className="bg-blue-50 dark:bg-blue-900/20 border-l-4 border-blue-500 p-4 rounded">
                    <h3 className="text-sm font-semibold text-blue-900 dark:text-blue-300 mb-2">
                      练习提示
                    </h3>
                    <ul className="list-disc list-inside space-y-1 text-sm text-blue-800 dark:text-blue-200">
                      {currentScenario.practiceTips.map((tip, index) => (
                        <li key={index}>{tip}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* 对话内容 */}
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-3">
                    对话示例
                  </h3>
                  <div className="bg-slate-50 dark:bg-slate-900 rounded-lg p-4 space-y-3 max-h-[600px] overflow-y-auto">
                    {(() => {
                      const dialogueText = currentScenario.dialogue;
                      
                      // 提取对话部分（支持多种 markdown 格式）
                      let dialogueSection = dialogueText;
                      
                      // 尝试匹配 "**3. Realistic Dialogue" 或 "### 3. Realistic Dialogue"
                      const dialogueMatch = dialogueText.match(/\*\*3\. Realistic Dialogue[\s\S]*?(?=\*\*4\.|### 4\.)/) ||
                                           dialogueText.match(/### 3\. Realistic Dialogue[\s\S]*?(?=### 4\.)/);
                      
                      if (dialogueMatch) {
                        dialogueSection = dialogueMatch[0];
                        // 移除标题本身
                        dialogueSection = dialogueSection.replace(/^(\*\*|###)3\. Realistic Dialogue[\s\S]*?(?=[A-Z])/, '');
                      }
                      
                      const lines = dialogueSection.split('\n');
                      const result: React.ReactNode[] = [];
                      
                      for (let i = 0; i < lines.length; i++) {
                        const line = lines[i];
                        const trimmedLine = line.trim();
                        
                        // 跳过空行和分隔符
                        if (!trimmedLine || trimmedLine === '---' || trimmedLine.startsWith('**') || 
                            trimmedLine.startsWith('###') || trimmedLine.startsWith('|') ||
                            trimmedLine.startsWith('-') || trimmedLine.startsWith('✅') ||
                            trimmedLine.startsWith('Let me know')) continue;
                        
                        // 检测是否是中文翻译行（包含中文括号）
                        const isChineseTranslation = trimmedLine.startsWith('（') && trimmedLine.endsWith('）');
                        
                        // 检测是否是说话人行（英文冒号，且不是表格或列表）
                        const isEnglishSpeaker = trimmedLine.includes(':') && !isChineseTranslation && 
                                                 !trimmedLine.startsWith('|') && !trimmedLine.startsWith('*') &&
                                                 !trimmedLine.startsWith('**') && !trimmedLine.startsWith('-');
                        
                        if (isEnglishSpeaker) {
                          // 解析英文说话人
                          const colonIndex = trimmedLine.indexOf(':');
                          const speaker = trimmedLine.substring(0, colonIndex).trim().replace(/[*_`]/g, '');
                          const text = trimmedLine.substring(colonIndex + 1).trim();
                          
                          result.push(
                            <div key={`english-${i}`} className="space-y-1">
                              <div className="flex gap-3">
                                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded text-sm font-medium shrink-0 min-w-[120px]">
                                  {speaker}
                                </span>
                                <span className="text-slate-700 dark:text-slate-300 leading-relaxed">{text}</span>
                              </div>
                            </div>
                          );
                        } else if (isChineseTranslation) {
                          // 显示中文翻译
                          result.push(
                            <div key={`chinese-${i}`} className="flex gap-3 pl-[140px]">
                              <span className="text-emerald-600 dark:text-emerald-400 text-sm font-medium shrink-0">
                                中文
                              </span>
                              <span className="text-emerald-700 dark:text-emerald-300 text-sm leading-relaxed">
                                {trimmedLine}
                              </span>
                            </div>
                          );
                        }
                      }
                      
                      return result;
                    })()}
                  </div>
                </div>

                {/* 关键短语 */}
                {currentScenario.keyPhrases && currentScenario.keyPhrases.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-3 flex items-center gap-2">
                      <BookOpen className="w-5 h-5 text-purple-500" />
                      关键短语
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {currentScenario.keyPhrases.map((phrase, index) => (
                        <span
                          key={index}
                          className="px-3 py-1 bg-purple-100 dark:bg-purple-900/30 text-purple-600 dark:text-purple-400 rounded-full text-sm"
                        >
                          {phrase}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                <hr className="border-slate-200 dark:border-slate-700" />

                {/* 用户练习区 */}
                <div>
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2 flex items-center gap-2">
                    <Lightbulb className="w-5 h-5 text-orange-500" />
                    练习你的表达
                  </h3>
                  <p className="text-sm text-slate-500 dark:text-slate-400 mb-3">
                    尝试用英语表达类似的内容，AI 会给你反馈和改进建议
                  </p>
                  
                  <textarea
                    value={userExpression}
                    onChange={(e) => setUserExpression(e.target.value)}
                    placeholder="在这里输入你的英语表达..."
                    className="w-full px-4 py-3 border border-slate-300 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-700 dark:text-white"
                    rows={4}
                  />
                  
                  <button
                    onClick={handleEvaluate}
                    disabled={evaluating || !userExpression.trim()}
                    className="mt-3 flex items-center gap-2 px-4 py-2.5 bg-green-500 hover:bg-green-600 disabled:bg-green-300 text-white rounded-lg transition-colors font-medium"
                  >
                    {evaluating ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        评估中...
                      </>
                    ) : (
                      <>
                        <CheckCircle2 className="w-4 h-4" />
                        评估我的表达
                      </>
                    )}
                  </button>
                </div>

                {/* 评估结果 */}
                {evaluationResult && (
                  <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-4 space-y-3">
                    <div className="flex items-center gap-2">
                      <CheckCircle2 className="w-5 h-5 text-green-500" />
                      <h3 className="font-semibold text-green-900 dark:text-green-300">
                        评估结果
                      </h3>
                    </div>
                    
                    {evaluationResult.score && (
                      <div>
                        <span className="text-sm font-medium text-green-800 dark:text-green-200">评分：</span>
                        <span className={`ml-2 px-2 py-1 rounded text-sm font-medium ${
                          evaluationResult.score >= 80 ? 'bg-green-100 text-green-700' :
                          evaluationResult.score >= 60 ? 'bg-blue-100 text-blue-700' :
                          'bg-orange-100 text-orange-700'
                        }`}>
                          {evaluationResult.score}/100
                        </span>
                      </div>
                    )}
                    
                    {evaluationResult.feedback && (
                      <div>
                        <span className="text-sm font-medium text-green-800 dark:text-green-200">反馈：</span>
                        <p className="mt-1 text-sm text-green-700 dark:text-green-300">
                          {evaluationResult.feedback}
                        </p>
                      </div>
                    )}
                    
                    {evaluationResult.suggestions && evaluationResult.suggestions.length > 0 && (
                      <div>
                        <span className="text-sm font-medium text-green-800 dark:text-green-200">改进建议：</span>
                        <ul className="mt-1 space-y-1 list-disc list-inside text-sm text-green-700 dark:text-green-300">
                          {evaluationResult.suggestions.map((suggestion: string, index: number) => (
                            <li key={index}>{suggestion}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                    
                    {evaluationResult.betterExpression && (
                      <div>
                        <span className="text-sm font-medium text-green-800 dark:text-green-200">更好的表达方式：</span>
                        <div className="mt-2 p-3 bg-green-100 dark:bg-green-900/30 rounded text-sm text-green-800 dark:text-green-200">
                          {evaluationResult.betterExpression}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 底部：常用短语参考 */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <BookOpen className="w-5 h-5 text-indigo-500" />
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            常用短语参考
          </h2>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Object.entries(phrases).map(([category, phraseList]) => (
            <div key={category} className="space-y-2">
              <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                {category.replace(/-/g, ' ')}
              </h3>
              <div className="space-y-1">
                {phraseList.slice(0, 5).map((phrase, index) => (
                  <div
                    key={index}
                    className="text-sm text-slate-700 dark:text-slate-300 pl-3 border-l-2 border-blue-300 dark:border-blue-600"
                  >
                    {phrase}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AgileEnglishPage;
