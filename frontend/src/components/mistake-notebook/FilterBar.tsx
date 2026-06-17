import { useState } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import type { QuestionFilterParams } from '../../types/mistakeNotebook';

interface FilterBarProps {
  skills: string[];
  onFilter: (params: QuestionFilterParams) => void;
  onReset: () => void;
}

export default function FilterBar({ skills, onFilter, onReset }: FilterBarProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [skillPoint, setSkillPoint] = useState('');
  const [difficulty, setDifficulty] = useState<number | ''>('');
  const [sourceType, setSourceType] = useState('');
  const [minMastery, setMinMastery] = useState<number | ''>('');
  const [maxMastery, setMaxMastery] = useState<number | ''>('');

  const hasActiveFilters = skillPoint || difficulty || sourceType || minMastery || maxMastery;

  const handleApply = () => {
    const params: QuestionFilterParams = {
      page: 0,
      size: 20,
    };
    if (skillPoint) params.skillPoint = skillPoint;
    if (difficulty !== '') params.difficulty = difficulty;
    if (sourceType) params.sourceType = sourceType;
    if (minMastery !== '') params.minMastery = minMastery;
    if (maxMastery !== '') params.maxMastery = maxMastery;
    onFilter(params);
  };

  const handleReset = () => {
    setSkillPoint('');
    setDifficulty('');
    setSourceType('');
    setMinMastery('');
    setMaxMastery('');
    onReset();
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 mb-6">
      {/* 主筛选栏 */}
      <div className="flex items-center gap-3 p-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <select
            value={skillPoint}
            onChange={(e) => setSkillPoint(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent appearance-none bg-white"
          >
            <option value="">全部技能点</option>
            {skills.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>

        <select
          value={difficulty}
          onChange={(e) => setDifficulty(e.target.value ? Number(e.target.value) : '')}
          className="px-4 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
        >
          <option value="">全部难度</option>
          {[1, 2, 3, 4, 5].map((d) => (
            <option key={d} value={d}>{d}星</option>
          ))}
        </select>

        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            isExpanded || hasActiveFilters
              ? 'bg-primary-50 text-primary-600 border border-primary-200'
              : 'bg-slate-50 text-slate-600 border border-slate-200 hover:bg-slate-100'
          }`}
        >
          <SlidersHorizontal className="w-4 h-4" />
          高级筛选
          {hasActiveFilters && (
            <span className="w-2 h-2 rounded-full bg-primary-500" />
          )}
        </button>

        <button
          onClick={handleApply}
          className="px-5 py-2 bg-primary-500 text-white rounded-lg text-sm font-medium hover:bg-primary-600 transition-colors"
        >
          搜索
        </button>

        {hasActiveFilters && (
          <button
            onClick={handleReset}
            className="flex items-center gap-1 px-3 py-2 text-slate-500 hover:text-slate-700 text-sm"
          >
            <X className="w-4 h-4" />
            重置
          </button>
        )}
      </div>

      {/* 高级筛选面板 */}
      {isExpanded && (
        <div className="border-t border-slate-100 px-4 py-4 grid grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-500 mb-1.5">题目来源</label>
            <select
              value={sourceType}
              onChange={(e) => setSourceType(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
            >
              <option value="">全部来源</option>
              <option value="AI_GENERATED">AI生成</option>
              <option value="MANUAL">手动添加</option>
              <option value="INTERVIEW_MISTAKE">面试错题</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-500 mb-1.5">最小掌握度</label>
            <input
              type="number"
              min={0}
              max={100}
              value={minMastery}
              onChange={(e) => setMinMastery(e.target.value ? Number(e.target.value) : '')}
              placeholder="0-100"
              className="w-full px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-500 mb-1.5">最大掌握度</label>
            <input
              type="number"
              min={0}
              max={100}
              value={maxMastery}
              onChange={(e) => setMaxMastery(e.target.value ? Number(e.target.value) : '')}
              placeholder="0-100"
              className="w-full px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>
      )}
    </div>
  );
}
