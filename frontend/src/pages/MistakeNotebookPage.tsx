import { useState, useEffect } from 'react';
import { mistakeNotebookApi } from '../api/mistakeNotebook';
import FilterBar from '../components/mistake-notebook/FilterBar';
import type { QuestionCard, RiskAssessment, QuestionFilterParams } from '../types/mistakeNotebook';

export default function MistakeNotebookPage() {
  const [activeTab, setActiveTab] = useState<'review' | 'list' | 'risk'>('review');
  const [card, setCard] = useState<QuestionCard | null>(null);
  const [flipped, setFlipped] = useState(false);
  const [riskAssessment, setRiskAssessment] = useState<RiskAssessment | null>(null);
  const [loading, setLoading] = useState(false);
  const [questions, setQuestions] = useState<QuestionCard[]>([]);
  const [skills, setSkills] = useState<string[]>([]);
  const [filterParams, setFilterParams] = useState<QuestionFilterParams>({ page: 0, size: 20 });
  const [generating, setGenerating] = useState(false);

  const loadNextCard = async () => {
    setLoading(true);
    try {
      const card = await mistakeNotebookApi.getNextReviewCard();
      setCard(card);
      setFlipped(false);
    } catch (e) {
      console.error('加载卡片失败', e);
    } finally {
      setLoading(false);
    }
  };

  const submitReview = async (mastery: 'again' | 'hard' | 'good' | 'easy') => {
    if (!card) return;
    await mistakeNotebookApi.submitCardReview(card.id, { mastery });
    await loadNextCard();
  };

  const generateRisk = async () => {
    setLoading(true);
    try {
      const assessment = await mistakeNotebookApi.generateRiskAssessment({
        interviewDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString(),
        jobDescription: 'Java后端开发工程师，要求精通Spring Boot、MySQL、Redis、微服务架构',
      });
      setRiskAssessment(assessment);
    } catch (e) {
      console.error('生成风险评估失败', e);
    } finally {
      setLoading(false);
    }
  };

  const loadQuestions = async (params: QuestionFilterParams = filterParams) => {
    setLoading(true);
    try {
      const page = await mistakeNotebookApi.filterQuestions(params);
      setQuestions(page.content || []);
    } catch (e) {
      console.error('加载题目列表失败', e);
    } finally {
      setLoading(false);
    }
  };

  const loadSkills = async () => {
    try {
      const skills = await mistakeNotebookApi.getUserSkills();
      setSkills(skills || []);
    } catch (e) {
      console.error('加载技能点失败', e);
    }
  };

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      await mistakeNotebookApi.generateFromResume();
      alert('题目生成任务已提交，请稍后刷新查看');
    } catch (e) {
      console.error('生成题库失败', e);
    } finally {
      setGenerating(false);
    }
  };

  useEffect(() => {
    loadSkills();
  }, []);

  useEffect(() => {
    if (activeTab === 'review') {
      loadNextCard();
    } else if (activeTab === 'list') {
      loadQuestions();
    }
  }, [activeTab]);

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">错题本 / 复盘中心</h1>

        {/* Tab导航 */}
        <div className="flex gap-2 mb-6">
          {[
            { key: 'review', label: '卡片复习' },
            { key: 'list', label: '题目列表' },
            { key: 'risk', label: '风险预测' },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as any)}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                activeTab === tab.key
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* 卡片复习 */}
        {activeTab === 'review' && (
          <div>
            {loading ? (
              <div className="text-center py-20 text-gray-500">加载中...</div>
            ) : card ? (
              <div className="relative">
                {/* 翻转卡片 */}
                <div
                  className="bg-white rounded-2xl shadow-lg p-8 min-h-[400px] cursor-pointer transition-transform duration-500"
                  style={{ perspective: '1000px' }}
                  onClick={() => setFlipped(!flipped)}
                >
                  <div className="text-sm text-gray-500 mb-4">
                    {card.skillPoint} · 难度{card.difficulty}
                  </div>
                  {!flipped ? (
                    <div>
                      <h3 className="text-xl font-semibold text-gray-900 mb-4">题目</h3>
                      <p className="text-gray-700 text-lg leading-relaxed">{card.questionText}</p>
                      <p className="text-gray-400 text-sm mt-8 text-center">点击翻转查看答案</p>
                    </div>
                  ) : (
                    <div>
                      <h3 className="text-xl font-semibold text-gray-900 mb-4">参考答案</h3>
                      <p className="text-gray-700 leading-relaxed mb-6">{card.answerText}</p>
                      {card.scoringPoints && (
                        <div className="mt-4">
                          <h4 className="font-medium text-gray-900 mb-2">得分要点</h4>
                          <ul className="list-disc list-inside text-gray-700 space-y-1">
                            {card.scoringPoints.map((p, i) => (
                              <li key={i}>{p}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>
                  )}
                </div>

                {/* 掌握度按钮 */}
                <div className="flex gap-3 mt-6 justify-center">
                  {[
                    { key: 'again', label: 'Again (完全不会)', color: 'bg-red-500' },
                    { key: 'hard', label: 'Hard (有点印象)', color: 'bg-orange-500' },
                    { key: 'good', label: 'Good (基本正确)', color: 'bg-blue-500' },
                    { key: 'easy', label: 'Easy (完美回答)', color: 'bg-green-500' },
                  ].map((btn) => (
                    <button
                      key={btn.key}
                      onClick={() => submitReview(btn.key as any)}
                      className={`${btn.color} text-white px-4 py-2 rounded-lg font-medium hover:opacity-90 transition-opacity`}
                    >
                      {btn.label}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-center py-20">
                <p className="text-gray-500 mb-4">暂无待复习卡片</p>
                <button
                  onClick={handleGenerate}
                  disabled={generating}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {generating ? '生成任务已提交...' : '生成我的专属题库'}
                </button>
              </div>
            )}
          </div>
        )}

        {/* 风险预测 */}
        {activeTab === 'risk' && (
          <div>
            <button
              onClick={generateRisk}
              className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 mb-6"
              disabled={loading}
            >
              {loading ? '生成中...' : '开始面试冲刺分析'}
            </button>

            {riskAssessment && (
              <div className="bg-white rounded-2xl shadow-lg p-8">
                <h2 className="text-xl font-bold text-gray-900 mb-4">面试风险评估</h2>

                {/* 风险热力图 */}
                <div className="mb-6">
                  <h3 className="font-semibold text-gray-800 mb-3">技能风险热力图</h3>
                  <div className="flex flex-wrap gap-2">
                    {Object.entries(riskAssessment.riskHeatmap).map(([skill, level]) => (
                      <span
                        key={skill}
                        className={`px-3 py-1 rounded-full text-sm font-medium ${
                          level === 'HIGH'
                            ? 'bg-red-100 text-red-700'
                            : level === 'MEDIUM'
                            ? 'bg-yellow-100 text-yellow-700'
                            : 'bg-green-100 text-green-700'
                        }`}
                      >
                        {skill}: {level}
                      </span>
                    ))}
                  </div>
                </div>

                {/* 死亡10题 */}
                <div>
                  <h3 className="font-semibold text-gray-800 mb-3">最可能考倒你的题目</h3>
                  <div className="space-y-4">
                    {riskAssessment.top10Questions?.map((q, i) => (
                      <div key={i} className="border-l-4 border-red-500 pl-4 py-2">
                        <p className="text-gray-900 font-medium">{i + 1}. {q.question}</p>
                        <p className="text-gray-600 text-sm mt-1">
                          <span className="font-medium">应急话术：</span>
                          {q.emergencyResponse}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* 题目列表 */}
        {activeTab === 'list' && (
          <div>
            <FilterBar
              skills={skills}
              onFilter={(params) => {
                setFilterParams(params);
                loadQuestions(params);
              }}
              onReset={() => {
                setFilterParams({ page: 0, size: 20 });
                loadQuestions({ page: 0, size: 20 });
              }}
            />

            {loading ? (
              <div className="text-center py-20 text-gray-500">加载中...</div>
            ) : questions.length > 0 ? (
              <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
                <table className="w-full">
                  <thead className="bg-slate-50 border-b border-slate-200">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">题目</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">技能点</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">难度</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">掌握度</th>
                      <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">来源</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {questions.map((q) => (
                      <tr key={q.id} className="hover:bg-slate-50 transition-colors">
                        <td className="px-6 py-4 text-sm text-gray-900 max-w-md truncate">{q.questionText}</td>
                        <td className="px-6 py-4">
                          <span className="inline-flex px-2 py-1 rounded-full text-xs font-medium bg-blue-50 text-blue-700">
                            {q.skillPoint}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-600">{'⭐'.repeat(q.difficulty)}</td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-2">
                            <div className="w-16 h-2 rounded-full bg-gray-200 overflow-hidden">
                              <div
                                className={`h-full rounded-full ${
                                  q.masteryScore >= 80 ? 'bg-green-500' :
                                  q.masteryScore >= 50 ? 'bg-yellow-500' : 'bg-red-500'
                                }`}
                                style={{ width: `${q.masteryScore}%` }}
                              />
                            </div>
                            <span className="text-xs text-gray-500">{q.masteryScore.toFixed(0)}%</span>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-xs text-gray-500">{q.sourceType}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="bg-white rounded-2xl shadow-lg p-8 text-center">
                <p className="text-gray-500 mb-4">暂无题目</p>
                <button
                  onClick={handleGenerate}
                  disabled={generating}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                  {generating ? '生成中...' : '生成我的专属题库'}
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
