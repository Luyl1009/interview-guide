import React, { useState, useEffect } from 'react';
import { History, TrendingUp, BookOpen, Calendar, Award, ChevronRight, Loader2, AlertCircle, X } from 'lucide-react';
import {
  getPracticeHistory,
  getCapabilityReport,
  getSessionRecords,
  type PracticeSessionDTO,
  type CapabilityReport,
  type PracticeRecordDTO,
} from '../api/agileEnglish';

const AgileEnglishHistoryPage: React.FC = () => {
  const [sessions, setSessions] = useState<PracticeSessionDTO[]>([]);
  const [report, setReport] = useState<CapabilityReport | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedSession, setSelectedSession] = useState<number | null>(null);
  const [sessionRecords, setSessionRecords] = useState<PracticeRecordDTO[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, [currentPage]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [historyRes, reportRes] = await Promise.all([
        getPracticeHistory(currentPage, 10),
        getCapabilityReport(),
      ]);
      setSessions(historyRes.sessions);
      setTotalPages(historyRes.totalPages);
      setReport(reportRes);
    } catch (error: any) {
      console.error('加载数据失败', error);
      setError(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleViewSession = async (sessionId: number) => {
    try {
      const records = await getSessionRecords(sessionId);
      setSessionRecords(records);
      setSelectedSession(sessionId);
    } catch (error: any) {
      setError(error.message || '加载会话记录失败');
    }
  };

  const handleCloseDetail = () => {
    setSelectedSession(null);
    setSessionRecords([]);
  };

  if (loading && sessions.length === 0) {
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
        <div className="relative bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-4 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm text-red-800 dark:text-red-300 font-medium">{error}</p>
          </div>
          <button onClick={() => setError(null)} className="text-red-400 hover:text-red-600">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* 页面标题 */}
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center text-white shadow-lg">
          <History className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            学习历史与进度
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            查看你的练习记录和成长轨迹
          </p>
        </div>
      </div>

      {/* 能力评估报告 */}
      {report && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
          <div className="flex items-center gap-2 mb-4">
            <Award className="w-5 h-5 text-yellow-500" />
            <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
              能力评估报告
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
              <div className="text-sm text-blue-600 dark:text-blue-400 mb-1">总会话数</div>
              <div className="text-3xl font-bold text-blue-700 dark:text-blue-300">
                {report.totalSessions}
              </div>
            </div>
            <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
              <div className="text-sm text-green-600 dark:text-green-400 mb-1">总练习次数</div>
              <div className="text-3xl font-bold text-green-700 dark:text-green-300">
                {report.totalPractices}
              </div>
            </div>
            <div className="bg-purple-50 dark:bg-purple-900/20 rounded-lg p-4">
              <div className="text-sm text-purple-600 dark:text-purple-400 mb-1">平均评分</div>
              <div className="text-3xl font-bold text-purple-700 dark:text-purple-300">
                {report.averageScore.toFixed(1)}
              </div>
            </div>
          </div>

          {/* 优势和改进建议 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {report.strengths.length > 0 && (
              <div className="bg-green-50 dark:bg-green-900/20 border-l-4 border-green-500 p-4 rounded">
                <h3 className="text-sm font-semibold text-green-900 dark:text-green-300 mb-2 flex items-center gap-2">
                  <TrendingUp className="w-4 h-4" />
                  优势领域
                </h3>
                <ul className="space-y-1">
                  {report.strengths.map((strength, index) => (
                    <li key={index} className="text-sm text-green-800 dark:text-green-200 flex items-start gap-2">
                      <ChevronRight className="w-4 h-4 mt-0.5 shrink-0" />
                      <span>{strength}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {report.areasForImprovement.length > 0 && (
              <div className="bg-orange-50 dark:bg-orange-900/20 border-l-4 border-orange-500 p-4 rounded">
                <h3 className="text-sm font-semibold text-orange-900 dark:text-orange-300 mb-2 flex items-center gap-2">
                  <BookOpen className="w-4 h-4" />
                  待提升领域
                </h3>
                <ul className="space-y-1">
                  {report.areasForImprovement.map((area, index) => (
                    <li key={index} className="text-sm text-orange-800 dark:text-orange-200 flex items-start gap-2">
                      <ChevronRight className="w-4 h-4 mt-0.5 shrink-0" />
                      <span>{area}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 练习历史列表 */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Calendar className="w-5 h-5 text-indigo-500" />
            <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
              练习历史
            </h2>
          </div>
          <div className="text-sm text-slate-500 dark:text-slate-400">
            第 {currentPage + 1} / {totalPages} 页
          </div>
        </div>

        {sessions.length === 0 ? (
          <div className="text-center py-12">
            <BookOpen className="w-16 h-16 text-slate-300 dark:text-slate-600 mx-auto mb-4" />
            <p className="text-slate-500 dark:text-slate-400">暂无练习记录</p>
            <p className="text-sm text-slate-400 dark:text-slate-500 mt-2">
              开始你的第一次敏捷英语练习吧！
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {sessions.map((session) => (
              <div
                key={session.id}
                className="border border-slate-200 dark:border-slate-700 rounded-lg p-4 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors cursor-pointer"
                onClick={() => handleViewSession(session.id)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded text-xs font-medium">
                        {session.scenarioType}
                      </span>
                      <span className="text-sm text-slate-600 dark:text-slate-400">
                        {session.role}
                      </span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-slate-500 dark:text-slate-400">
                      <span>练习次数: {session.practiceCount}</span>
                      <span>平均分: {session.averageScore.toFixed(1)}</span>
                      <span>{new Date(session.createdAt).toLocaleDateString('zh-CN')}</span>
                    </div>
                  </div>
                  <ChevronRight className="w-5 h-5 text-slate-400" />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 分页控制 */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-6">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300"
            >
              上一页
            </button>
            <span className="text-sm text-slate-600 dark:text-slate-400">
              {currentPage + 1} / {totalPages}
            </span>
            <button
              onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
              disabled={currentPage >= totalPages - 1}
              className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300"
            >
              下一页
            </button>
          </div>
        )}
      </div>

      {/* 会话详情弹窗 */}
      {selectedSession !== null && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-slate-800 rounded-xl max-w-4xl w-full max-h-[80vh] overflow-hidden flex flex-col">
            <div className="p-6 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-white">
                练习记录详情
              </h3>
              <button
                onClick={handleCloseDetail}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="flex-1 overflow-y-auto p-6">
              {sessionRecords.length === 0 ? (
                <div className="text-center py-12">
                  <BookOpen className="w-16 h-16 text-slate-300 dark:text-slate-600 mx-auto mb-4" />
                  <p className="text-slate-500 dark:text-slate-400">暂无练习记录</p>
                  <p className="text-sm text-slate-400 dark:text-slate-500 mt-2">
                    该会话尚未进行任何对话练习
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {sessionRecords.map((record) => (
                    <div key={record.id} className="border border-slate-200 dark:border-slate-700 rounded-lg p-4">
                      <div className="flex items-center justify-between mb-3">
                        <span className="text-sm font-medium text-slate-600 dark:text-slate-400">
                          第 {record.roundNumber} 轮
                        </span>
                        {record.score !== null && record.score !== undefined && (
                          <span className={`px-2 py-1 rounded text-xs font-medium ${
                            record.score >= 80 ? 'bg-green-100 text-green-700' :
                            record.score >= 60 ? 'bg-blue-100 text-blue-700' :
                            'bg-orange-100 text-orange-700'
                          }`}>
                            {record.score}分
                          </span>
                        )}
                      </div>
                      
                      <div className="space-y-2">
                        <div>
                          <div className="text-xs text-slate-500 dark:text-slate-400 mb-1">你的表达：</div>
                          <div className="text-sm text-slate-700 dark:text-slate-300 bg-slate-50 dark:bg-slate-900 p-3 rounded">
                            {record.userExpression}
                          </div>
                        </div>
                        
                        {record.aiFeedback && (
                          <div>
                            <div className="text-xs text-slate-500 dark:text-slate-400 mb-1">AI反馈：</div>
                            <div className="text-sm text-slate-700 dark:text-slate-300 bg-blue-50 dark:bg-blue-900/20 p-3 rounded">
                              {record.aiFeedback}
                            </div>
                          </div>
                        )}
                        
                        {record.betterExpression && (
                          <div>
                            <div className="text-xs text-slate-500 dark:text-slate-400 mb-1">更好的表达：</div>
                            <div className="text-sm text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-900/20 p-3 rounded">
                              {record.betterExpression}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AgileEnglishHistoryPage;
