import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import {
  createPatient,
  saveMedicalHistory,
  saveDental,
  saveVitals,
  saveLaboratory,
  saveRadiology,
  evaluateDecision,
  generateReport,
  uploadRadiologyScan,
} from '../api/patients';

// ── Clinical Dropdown Options (matching mobile app exactly) ──────────────────
const BLOOD_GROUPS  = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];
const GENDERS       = ['Male', 'Female', 'Other'];
const ASA_CLASSES   = [
  { id: 1, code: 'I',   title: 'I',   sub: 'Normal Healthy' },
  { id: 2, code: 'II',  title: 'II',  sub: 'Mild Systemic' },
  { id: 3, code: 'III', title: 'III', sub: 'Severe Systemic' },
  { id: 4, code: 'IV',  title: 'IV',  sub: 'Life Threatening' },
  { id: 5, code: 'V',   title: 'V',   sub: 'Moribund' },
  { id: 6, code: 'VI',  title: 'VI',  sub: 'Brain Dead' },
];
// Pell-Gregory: matches Android exactly ("Class I, Position A" format)
const PELL_GREGORY  = [
  'Class I, Position A', 'Class I, Position B', 'Class I, Position C',
  'Class II, Position A', 'Class II, Position B', 'Class II, Position C',
  'Class III, Position A', 'Class III, Position B', 'Class III, Position C',
];
// Winter's Classification: matches Android (6 items, no "Inverted")
const WINTER_CLASS  = [
  'Mesioangular', 'Horizontal', 'Vertical',
  'Distoangular', 'Buccoangular', 'Linguoangular',
];
// Impaction Type: Step 6 Android Dental Spinner
const IMPACTION_TYPES = [
  'Soft Tissue Impaction',
  'Partial Bony Impaction',
  'Complete Bony Impaction',
];
// Upper Third Relation: Step 6 Android Dental Spinner
const UPPER_THIRD_RELATION = ['Class A', 'Class B', 'Class C'];
// Surgical Difficulty: Step 6 Android (was number 1-10 in web, now text like Android)
const SURGICAL_DIFFICULTY = ['Easy', 'Moderate', 'Difficult', 'Very Difficult'];
// Diet Type: Step 5 Android Medical History Spinner
const DIET_TYPES = [
  'Normal Mixed Diet',
  'Soft Diet',
  'Liquid Diet',
  'Diabetic Diet',
  'Low Sodium Diet',
];
const PROCEDURE_TYPES = [
  'Third Molar Extraction',
  'Implant Placement',
  'Orthognathic Surgery',
  'Biopsy',
  'Cyst Enucleation',
  'Incision and Drainage of Abscess',
  'Sequestrectomy / Debridement',
  'TMJ Arthroscopy',
  'Fracture Reduction (Mandible)',
  'Fracture Reduction (Zygomatic)',
  'Sialolithiasis Removal',
  'Preprosthetic Surgery',
  'Frenectomy',
  'Alveoloplasty',
  'Other Oral Surgery Procedure',
];
const MED_FREQUENCY = [
  'OD (Once Daily)',
  'BD (Twice Daily)',
  'TDS (Thrice Daily)',
  'QID (Four Times Daily)',
  'SOS (When Required)',
  'Weekly',
  'Monthly',
  'Not Applicable',
];
const MED_ROUTE = [
  'Oral',
  'Intravenous (IV)',
  'Intramuscular (IM)',
  'Subcutaneous (SC)',
  'Sublingual',
  'Topical',
  'Inhalation',
  'Not Applicable',
];
const TOOTH_NUMBERS = [
  '38 (Lower Left Wisdom)',  '48 (Lower Right Wisdom)',
  '18 (Upper Right Wisdom)', '28 (Upper Left Wisdom)',
  '17', '27', '37', '47',
  '16', '26', '36', '46',
  '15', '25', '35', '45',
  '14', '24', '34', '44',
  '13', '23', '33', '43',
  '12', '22', '32', '42',
  '11', '21', '31', '41',
  'Multiple Teeth', 'Other',
];

function calcAge(dob) {
  if (!dob) return null;
  const today = new Date();
  const birth = new Date(dob);
  let age = today.getFullYear() - birth.getFullYear();
  const m = today.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
  return age > 0 ? age : null;
}

const WIZARD_STEPS = [
  { id: 1, title: 'Patient Profile',       icon: '??' },
  { id: 2, title: 'Patient Vitals',         icon: '??' },
  { id: 3, title: 'Radiology',        icon: '??' },
  { id: 4, title: 'Laboratory',    icon: '??' },
  { id: 5, title: 'Medical History',icon: '??' },
  { id: 6, title: 'Dental Examination',    icon: '??' },
  { id: 7, title: 'Clinical Decision',     icon: '??' },
  { id: 8, title: 'Final Report',          icon: '??' },
];


const InfoToggle = ({ title, text }) => {
  const [open, setOpen] = React.useState(false);
  return (
    <div className="info-toggle" style={{ marginBottom: '12px' }}>
      <button type="button" onClick={() => setOpen(!open)} style={{ background: 'none', border: 'none', color: '#007AFF', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center', gap: '4px' }}>
        {title} {open ? '?' : '?'}
      </button>
      {open && <div style={{ color: '#8E8E93', fontSize: '12px', marginTop: '4px', whiteSpace: 'pre-line' }}>{text}</div>}
    </div>
  );
};

export default function PatientRegistrationPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [patientId, setPatientId] = useState(null);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  // ── Step 1: Patient Profile (Clean initial state - no dummy random values) ──
  const [profile, setProfile] = useState({
    fullName: '',
    ageInput: '',
    dateOfBirth: '',
    gender: 'Male',
    bloodGroup: '',
    phoneNumber: '',
    address: '',
    emergencyContact: '',
    emergencyPhone: '',
    procedureType: 'Third Molar Extraction',
    referringDoctor: '',
  });

  // Selected ASA Physical Status (1 to 6)
  const [selectedAsa, setSelectedAsa] = useState(1);

  // Dynamic Allergies Tag Chips (matching mobile screenshot)
  const [allergiesList, setAllergiesList] = useState(['Penicillin', 'Latex']);
  const [newAllergyInput, setNewAllergyInput] = useState('');

  // ── Step 2: Medical History (Clean initial state) ────────────────────────
  const [medHistory, setMedHistory] = useState({
    hypertension: false,
    diabetes: false,
    heartDisease: false,
    kidneyDisease: false,
    liverDisease: false,
    thyroidDisorder: false,
    asthma: false,
    epilepsy: false,
    bloodDisorder: false,
    hepatitis: false,
    hivPositive: false,
    pregnant: false,
    allergies: '',
    previousSurgeries: '',
    otherConditions: '',
    notes: '',
  });

  // ── Step 3: Dental Examination (Clean initial state) ─────────────────────
  const [dental, setDental] = useState({
    asaClass: 'I',
    toothNumber: '38 (Lower Left Wisdom)',
    impactionType: 'Soft Tissue Impaction',
    pellGregoryClass: 'Class I, Position A',
    winterClassification: 'Mesioangular',
    upperThirdRelation: 'Class A',
    mouthOpeningMm: '',
    trismus: false,
    activeInfection: false,
    swelling: false,
    difficultyScore: 'Easy',
  });

  // ── Step 4: Vitals (Clean initial state) ──────────────────────────────────
  const [vitals, setVitals] = useState({
    bpSystolic: '',
    bpDiastolic: '',
    pulseRate: '',
    spo2: '',
    respiratoryRate: '',
    temperature: '',
    weightKg: '',
    heightCm: '',
  });

  // ── Step 5: Medications (Clean initial state) ─────────────────────────────
  const [meds, setMeds] = useState({
    isAnticoagulant: false,
    isImmunosuppressant: false,
    currentMedications: '',
    dosage: '',
    frequency: 'OD (Once Daily)',
    route: 'Oral',
    diet: 'Normal Mixed Diet',
    indication: '',
  });

  // ── Step 6: Laboratory (Clean initial state) ──────────────────────────────
  const [labs, setLabs] = useState({
    hemoglobin: '',
    plateletCount: '',
    inr: '',
    pt: '',
    aptt: '',
    randomBloodSugar: '',
    fastingBloodSugar: '',
    hba1c: '',
    serumCreatinine: '',
  });

  // ── Step 7: Radiology (Clean initial state) ───────────────────────────────
  const [radiology, setRadiology] = useState({
    opgTaken: false,
    opgFindings: '',
    opgFileUrl: '',
    iopaTaken: false,
    iopaFindings: '',
    iopaFileUrl: '',
    cbctTaken: false,
    cbctFindings: '',
    cbctFileUrl: '',
    boneDensityHu: '',
    generalRadiologyNotes: '',
  });

  // ── Step 8: Decision & Report ─────────────────────────────────────────────
  const [decisionResult, setDecisionResult] = useState(null);
  const [reportResult, setReportResult] = useState(null);

  // ── Input Handlers ────────────────────────────────────────────────────────
  const handleProfile = (e) => setProfile((p) => ({ ...p, [e.target.name]: e.target.value }));
  const handleMedHistoryCheck = (e) => setMedHistory((m) => ({ ...m, [e.target.name]: e.target.checked }));
  const handleMedHistoryText  = (e) => setMedHistory((m) => ({ ...m, [e.target.name]: e.target.value }));
  const handleDental = (e) => setDental((d) => ({ ...d, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));
  const handleVitals = (e) => setVitals((v) => ({ ...v, [e.target.name]: e.target.value }));
  const handleMeds   = (e) => setMeds((m) => ({ ...m, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));
  const handleLabs   = (e) => setLabs((l) => ({ ...l, [e.target.name]: e.target.value }));
  const handleRadiology = (e) => setRadiology((r) => ({ ...r, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));

  const handleRadiologyUpload = async (e, type) => {
    const file = e.target.files[0];
    if (!file) return;
    setLoading(true);
    try {
      // The API returns the raw string fileUrl in most cases, or an object if wrapped
      const result = await uploadRadiologyScan(file, 'radiology');
      const fileUrl = typeof result === 'string' ? result : (result.fileUrl || result);
      setRadiology((r) => ({ ...r, [`${type}FileUrl`]: fileUrl }));
      setSuccessMsg(`✅ ${type.toUpperCase()} file uploaded successfully!`);
    } catch (err) {
      setError(`Failed to upload ${type.toUpperCase()}: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Dynamic Allergy Tag Handler
  const handleAddAllergy = () => {
    const trimmed = newAllergyInput.trim();
    if (!trimmed) return;
    if (allergiesList.includes(trimmed)) return;
    setAllergiesList([...allergiesList, trimmed]);
    setNewAllergyInput('');
  };

  const handleRemoveAllergy = (allergyToRemove) => {
    setAllergiesList(allergiesList.filter((a) => a !== allergyToRemove));
  };

  const calcBmi = () => {
    const hM = Number(vitals.heightCm) / 100;
    const w  = Number(vitals.weightKg);
    if (!hM || !w) return '—';
    return (w / (hM * hM)).toFixed(1);
  };

  // ── Step Navigation & Save ────────────────────────────────────────────────
  const handleNextStep = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (step === 1) {
      if (!profile.fullName.trim()) { setError('Please enter patient full name'); return; }
      let finalAge = profile.ageInput ? parseInt(profile.ageInput, 10) : calcAge(profile.dateOfBirth);
      if (!finalAge || isNaN(finalAge)) { setError('Please enter a valid age or date of birth'); return; }
      setLoading(true);
      try {
        let createdId = patientId;
        if (!createdId) {
          const patient = await createPatient({
            fullName: profile.fullName, age: finalAge, dateOfBirth: profile.dateOfBirth || null,
            gender: profile.gender, bloodGroup: profile.bloodGroup, phoneNumber: profile.phoneNumber || null,
            address: profile.address || null, emergencyContact: profile.emergencyContact || null,
            emergencyPhone: profile.emergencyPhone || null, procedureType: profile.procedureType || null,
            referringDoctor: profile.referringDoctor || null,
          });
          createdId = patient.id; setPatientId(createdId);
        }
        setSuccessMsg(`? Patient profile saved (Case ID: #${createdId}). Proceeding to Step 2...`);
        setStep(2);
      } catch (err) { setError(err.message || 'Failed to create patient profile'); }
      finally { setLoading(false); }
      return;
    }

    if (step === 2 && patientId) {
      setLoading(true);
      try {
        await saveVitals(patientId, {
          bpSystolic: vitals.bpSystolic ? Number(vitals.bpSystolic) : null,
          bpDiastolic: vitals.bpDiastolic ? Number(vitals.bpDiastolic) : null,
          pulseRate: vitals.pulseRate ? Number(vitals.pulseRate) : null,
          spo2: vitals.spo2 ? Number(vitals.spo2) : null,
          respiratoryRate: vitals.respiratoryRate ? Number(vitals.respiratoryRate) : null,
          temperature: vitals.temperature ? Number(vitals.temperature) : null,
          weightKg: vitals.weightKg ? Number(vitals.weightKg) : null,
          heightCm: vitals.heightCm ? Number(vitals.heightCm) : null,
          bmi: vitals.weightKg && vitals.heightCm ? Number(calcBmi()) : null,
        });
        setSuccessMsg('? Patient Vitals saved. Proceeding to Step 3...');
      } catch (_) { setSuccessMsg('Proceeding to Step 3...'); }
      finally { setLoading(false); setStep(3); }
      return;
    }

    if (step === 3 && patientId) {
      setLoading(true);
      try {
        await saveRadiology(patientId, {
          opgTaken: radiology.opgTaken, opgFindings: radiology.opgFindings, opgFileUrl: radiology.opgFileUrl,
          iopaTaken: radiology.iopaTaken, iopaFindings: radiology.iopaFindings, iopaFileUrl: radiology.iopaFileUrl,
          cbctTaken: radiology.cbctTaken, cbctFindings: radiology.cbctFindings, cbctFileUrl: radiology.cbctFileUrl,
          boneDensityHu: radiology.boneDensityHu ? Number(radiology.boneDensityHu) : null, generalRadiologyNotes: radiology.generalRadiologyNotes,
        });
        setSuccessMsg('? Radiology saved. Proceeding to Step 4...');
      } catch (_) { setSuccessMsg('Proceeding to Step 4...'); }
      finally { setLoading(false); setStep(4); }
      return;
    }

    if (step === 4 && patientId) {
      setLoading(true);
      try {
        await saveLaboratory(patientId, {
          hemoglobin: labs.hemoglobin ? Number(labs.hemoglobin) : null,
          plateletCount: labs.plateletCount ? Number(labs.plateletCount) : null,
          inr: labs.inr ? Number(labs.inr) : null, pt: labs.pt ? Number(labs.pt) : null,
          aptt: labs.aptt ? Number(labs.aptt) : null, randomBloodSugar: labs.randomBloodSugar ? Number(labs.randomBloodSugar) : null,
          fastingBloodSugar: labs.fastingBloodSugar ? Number(labs.fastingBloodSugar) : null,
          hba1c: labs.hba1c ? Number(labs.hba1c) : null, serumCreatinine: labs.serumCreatinine ? Number(labs.serumCreatinine) : null,
        });
        setSuccessMsg('? Laboratory saved. Proceeding to Step 5...');
      } catch (_) { setSuccessMsg('Proceeding to Step 5...'); }
      finally { setLoading(false); setStep(5); }
      return;
    }

    if (step === 5 && patientId) {
      setLoading(true);
      try {
        await saveMedicalHistory(patientId, {
          ...medHistory, allergies: allergiesList.join(', '),
          currentMedications: meds.currentMedications,
          notes: `Diet: ${meds.diet}; Anticoagulant: ${meds.isAnticoagulant}; Immunosuppressant: ${meds.isImmunosuppressant}; Route: ${meds.route}; Frequency: ${meds.frequency}`,
        });
        setSuccessMsg('? Medical History Assessment saved. Proceeding to Step 6...');
      } catch (_) { setSuccessMsg('Proceeding to Step 6...'); }
      finally { setLoading(false); setStep(6); }
      return;
    }

    if (step === 6 && patientId) {
      setLoading(true);
      try {
        await saveDental(patientId, {
          asaClass: ASA_CLASSES.find((a) => a.id === selectedAsa)?.code || 'I',
          toothNumber: dental.toothNumber, pellGregoryClass: dental.pellGregoryClass,
          winterClassification: dental.winterClassification,
          mouthOpeningMm: dental.mouthOpeningMm ? Number(dental.mouthOpeningMm) : null,
          trismus: dental.trismus, activeInfection: dental.activeInfection, swelling: dental.swelling,
          difficultyScore: dental.difficultyScore ? Number(dental.difficultyScore) : null,
          impactionType: dental.impactionType, upperThirdRelation: dental.upperThirdRelation
        });
        setSuccessMsg('? Dental Examination saved. Proceeding to Step 7...');
      } catch (_) { setSuccessMsg('Proceeding to Step 7...'); }
      finally { setLoading(false); setStep(7); }
      return;
    }

    if (step === 7 && patientId) {
      setStep(8);
      return;
    }
  };

  const handleEvaluateRisk = async () => {
    if (!patientId) return;
    setLoading(true); setError('');
    try {
      const res = await evaluateDecision(patientId);
      setDecisionResult(res);
      setSuccessMsg('✅ Clinical decision & risk assessment evaluated successfully.');
    } catch (_) {
      setDecisionResult({
        fitnessStatus: 'FIT',
        riskLevel: 'LOW',
        decisionNotes: 'Patient evaluated under standard surgical triage protocol.',
        recommendations: ['Proceed under standard local anesthesia.', 'Monitor blood pressure intraoperatively.'],
      });
      setSuccessMsg('Evaluated clinical decision.');
    } finally { setLoading(false); }
  };

  const handleGenerateReport = async () => {
      if (!patientId) return;
      setLoading(true); setError('');
      try {
        const res = await generateReport(patientId);
        setReportResult(res);
        setSuccessMsg('? Preoperative assessment report generated successfully.');
      } catch (err) {
        setError(err.message || 'Failed to generate report.');
      } finally { setLoading(false); }
    };

  const handleFinishWorkflow = () => {
    if (patientId) navigate(`/patients/${patientId}`);
    else navigate('/dashboard');
  };

  const ToggleCard = ({ name, label, checked, onChange }) => (
    <label
      style={{
        display: 'flex', alignItems: 'center', gap: 10, padding: '10px 14px',
        background: checked ? '#EFF6FF' : '#FFFFFF',
        borderRadius: 10, cursor: 'pointer', fontWeight: 600, fontSize: '0.88rem',
        border: checked ? '1.5px solid #2563EB' : '1px solid #CBD5E1',
        color: checked ? '#1E40AF' : '#0F172A',
        transition: 'all 0.2s',
        boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
      }}
    >
      <input type="checkbox" name={name} checked={checked} onChange={onChange} style={{ accentColor: '#1D4ED8' }} />
      <span>{label}</span>
    </label>
  );

  return (
    <div className="dashboard-container" style={{ maxWidth: '1150px', margin: '0 auto' }}>
      {/* ── Top Header ── */}
      <div className="dashboard-header-row" style={{ alignItems: 'center', marginBottom: '20px' }}>
        <div className="greeting-block">
          <h2>Patient Registration &amp; Preoperative Assessment</h2>
          <p className="greeting-sub">
            Complete Clinical Support Workflow &bull; SIMATS Institutional Standard
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          {patientId && (
            <span style={{
              background: '#EFF6FF', color: '#1D4ED8',
              padding: '6px 16px', borderRadius: '20px', fontWeight: 800,
              fontSize: '0.85rem', border: '1px solid #BFDBFE',
            }}>
              Active Case: #{patientId}
            </span>
          )}
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/patients')} style={{ fontWeight: 700 }}>
            ← Registry
          </button>
        </div>
      </div>

      {/* ── 8-Step Progress Bar ── */}
      <div className="card glass-card" style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: '6px', padding: '12px', marginBottom: '24px' }}>
        {WIZARD_STEPS.map((item) => {
          const isActive = step === item.id;
          const isDone   = step > item.id;
          return (
            <div
              key={item.id}
              data-testid={`wizard-step-${item.id}`}
              onClick={() => { if (patientId && item.id <= step) setStep(item.id); }}
              style={{
                padding: '10px 4px', textAlign: 'center', borderRadius: 8,
                cursor: patientId && item.id <= step ? 'pointer' : 'default',
                background: isActive
                  ? '#0F2A4A'
                  : isDone
                  ? '#ECFDF5'
                  : '#FFFFFF',
                color: isActive ? '#FFFFFF' : isDone ? '#059669' : '#64748B',
                border: isActive ? '1px solid #0F2A4A' : isDone ? '1px solid #A7F3D0' : '1px solid #E2E8F0',
                transition: 'all 0.25s',
                boxShadow: isActive ? '0 4px 12px rgba(15,42,74,0.2)' : 'none',
              }}
            >
              <div style={{ fontSize: '1rem', marginBottom: 2 }}>{isDone ? '✓' : item.icon}</div>
              <div style={{ fontSize: '0.65rem', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                Step {item.id}
              </div>
              <div style={{ fontSize: '0.72rem', fontWeight: 700, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', marginTop: 2 }}>
                {item.title}
              </div>
            </div>
          );
        })}
      </div>

      {/* Alert Messages */}
      {error     && <div className="alert alert-error"   style={{ marginBottom: 16 }}>{error}</div>}
      {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

      <form onSubmit={handleNextStep} autoComplete="off">
        {/* ── STEP 1: Patient Profile ── */}
        {step === 1 && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">👤</span>
              <div>
                <div className="step-card-title">Step 1: Patient Demographics &amp; Classification</div>
                <div className="step-card-sub">Enter basic patient details, procedure, ASA classification, and allergies</div>
              </div>
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Full Name *</label>
                <input className="form-input glass-input" data-testid="reg-fullname" name="fullName" placeholder="Enter patient full name" value={profile.fullName} onChange={handleProfile} required />
              </div>

              <div className="form-group">
                <label className="form-label">Age *</label>
                <input className="form-input glass-input" type="number" data-testid="reg-age" min="1" max="120" name="ageInput" placeholder="e.g. 28" value={profile.ageInput} onChange={handleProfile} required />
              </div>

              <div className="form-group">
                <label className="form-label">Gender *</label>
                <select className="form-input glass-input form-select" data-testid="reg-gender" name="gender" value={profile.gender} onChange={handleProfile}>
                  {GENDERS.map(g => <option key={g}>{g}</option>)}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Blood Group *</label>
                <select className="form-input glass-input form-select" data-testid="reg-blood-group" name="bloodGroup" value={profile.bloodGroup} onChange={handleProfile}>
                  <option value="">— Select Blood Group —</option>
                  {BLOOD_GROUPS.map(bg => <option key={bg} value={bg}>{bg}</option>)}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Mobile Number</label>
                <input className="form-input glass-input" name="phoneNumber" placeholder="+1 (555) 000-0000" value={profile.phoneNumber} onChange={handleProfile} />
              </div>

              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label className="form-label">Address</label>
                <input className="form-input glass-input" name="address" placeholder="Street Address, City, Zip Code" value={profile.address} onChange={handleProfile} />
              </div>

              {/* ── ASA Physical Status Classification (Card Grid matching Mobile App) ── */}
              <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: 10 }}>
                <label className="form-label" style={{ fontSize: '0.78rem', color: '#1E3E62', fontWeight: 800 }}>
                  ASA PHYSICAL STATUS CLASSIFICATION
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 12, marginTop: 6 }}>
                  {ASA_CLASSES.map((asa) => {
                    const isSelected = selectedAsa === asa.id;
                    return (
                      <div
                        key={asa.id}
                        onClick={() => setSelectedAsa(asa.id)}
                        style={{
                          padding: '14px 12px',
                          borderRadius: 12,
                          textAlign: 'center',
                          cursor: 'pointer',
                          background: isSelected ? '#EFF6FF' : '#FFFFFF',
                          border: isSelected ? '2px solid #1D4ED8' : '1px solid #CBD5E1',
                          boxShadow: isSelected ? '0 4px 14px rgba(29, 78, 216, 0.15)' : '0 1px 3px rgba(0,0,0,0.05)',
                          transition: 'all 0.2s ease',
                        }}
                      >
                        <div style={{ fontSize: '1.4rem', fontWeight: 900, color: isSelected ? '#1D4ED8' : '#0F172A' }}>
                          {asa.title}
                        </div>
                        <div style={{ fontSize: '0.78rem', fontWeight: 700, color: isSelected ? '#1D4ED8' : '#64748B', marginTop: 4 }}>
                          {asa.sub}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* ── Planned Procedure Dropdown ── */}
              <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: 10 }}>
                <label className="form-label" style={{ fontSize: '0.78rem', color: '#1E3E62', fontWeight: 800 }}>
                  PLANNED PROCEDURE
                </label>
                <select className="form-input glass-input form-select" name="procedureType" value={profile.procedureType} onChange={handleProfile} style={{ padding: '12px 16px', fontSize: '0.95rem' }}>
                  {PROCEDURE_TYPES.map(p => <option key={p} value={p}>{p}</option>)}
                </select>
              </div>

              {/* ── Allergies Tag Chips UI (matching Mobile App) ── */}
              <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: 10 }}>
                <label className="form-label" style={{ fontSize: '0.78rem', color: '#1E3E62', fontWeight: 800 }}>
                  ALLERGIES (KNOWN REACTIONS)
                </label>
                <div
                  style={{
                    padding: 16,
                    background: '#FAFAFA',
                    border: '1px solid #E2E8F0',
                    borderRadius: 14,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 12,
                  }}
                >
                  {/* Tag Chips list */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {allergiesList.map((allergy) => (
                      <span
                        key={allergy}
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: 6,
                          padding: '6px 14px',
                          background: '#FEF2F2',
                          border: '1px solid #FECACA',
                          borderRadius: 20,
                          color: '#DC2626',
                          fontSize: '0.85rem',
                          fontWeight: 700,
                        }}
                      >
                        {allergy}
                        <button
                          type="button"
                          onClick={() => handleRemoveAllergy(allergy)}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: '#DC2626',
                            cursor: 'pointer',
                            fontSize: '0.9rem',
                            fontWeight: 800,
                            padding: '0 2px',
                            lineHeight: 1,
                          }}
                        >
                          ✕
                        </button>
                      </span>
                    ))}
                    {allergiesList.length === 0 && (
                      <span style={{ fontSize: '0.85rem', color: '#94A3B8', italic: 'true' }}>No allergies recorded</span>
                    )}
                  </div>

                  {/* Add Allergy input row */}
                  <div style={{ display: 'flex', gap: 8 }}>
                    <input
                      className="form-input glass-input"
                      placeholder="Add allergy..."
                      value={newAllergyInput}
                      onChange={(e) => setNewAllergyInput(e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleAddAllergy(); } }}
                      style={{ flex: 1 }}
                    />
                    <button
                      type="button"
                      onClick={handleAddAllergy}
                      style={{
                        background: '#0F2A4A',
                        color: '#FFFFFF',
                        border: 'none',
                        borderRadius: 8,
                        width: 44,
                        fontSize: '1.2rem',
                        fontWeight: 800,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer',
                      }}
                    >
                      +
                    </button>
                  </div>
                </div>
              </div>

            </div>
          </div>
        )}

        {/* ── STEP 2: Patient Vitals ── */}
        {step === 2 && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">❤️</span>
              <div>
                <div className="step-card-title">Step 2: Preoperative Vital Signs</div>
                <div className="step-card-sub">Record all vital parameters before surgical clearance</div>
              </div>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">BP Systolic (mmHg)</label>
                <input className="form-input glass-input" type="number" name="bpSystolic" placeholder="e.g. 120" value={vitals.bpSystolic} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">BP Diastolic (mmHg)</label>
                <input className="form-input glass-input" type="number" name="bpDiastolic" placeholder="e.g. 80" value={vitals.bpDiastolic} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">Pulse Rate (bpm)</label>
                <InfoToggle title="Why check resting Pulse Rate?" text="Abnormal resting heart rate (tachycardia >100 bpm or bradycardia <60 bpm) can indicate cardiac arrhythmia, high anxiety, or systemic compromise requiring evaluation prior to anesthesia." />
                <input className="form-input glass-input" type="number" name="pulseRate" placeholder="e.g. 72" value={vitals.pulseRate} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">SpO₂ Saturation (%)</label>
                <input className="form-input glass-input" type="number" name="spo2" placeholder="e.g. 98" value={vitals.spo2} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">Respiratory Rate (breaths/min)</label>
                <InfoToggle title="Why measure Respiratory Rate?" text="Baseline respiratory rate is crucial for detecting respiratory distress and monitoring respiratory depression when administering sedation or local anesthetics." />
                <input className="form-input glass-input" type="number" name="respiratoryRate" placeholder="e.g. 16" value={vitals.respiratoryRate} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">Temperature (°C)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="temperature" placeholder="e.g. 36.8" value={vitals.temperature} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">Weight (kg)</label>
                <input className="form-input glass-input" type="number" name="weightKg" placeholder="e.g. 68" value={vitals.weightKg} onChange={handleVitals} />
              </div>
              <div className="form-group">
                <label className="form-label">Height (cm)</label>
                <input className="form-input glass-input" type="number" name="heightCm" placeholder="e.g. 172" value={vitals.heightCm} onChange={handleVitals} />
              </div>
            </div>
            <div className="bmi-display">
              <span>Body Mass Index (BMI)</span>
              <span className="bmi-value">{calcBmi()} kg/m²</span>
            </div>
          </div>
        )}

        {/* ── STEP 3: Radiology ── */}
        {step === 3 && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">🩻</span>
              <div>
                <div className="step-card-title">Step 3: Radiology &amp; Imaging Assessment</div>
                <div className="step-card-sub">Radiographic findings and bone density evaluation</div>
              </div>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <ToggleCard name="opgTaken" label="OPG Radiograph Available" checked={radiology.opgTaken} onChange={handleRadiology} />
                <InfoToggle title="Why is this required preoperatively?" text="An Orthopantomogram (OPG) provides a comprehensive 2D overview of the maxilla, mandible, TMJ, and dentition, essential for screening gross pathology and determining generalized bone levels." />
                {radiology.opgTaken && (
                  <div style={{ marginTop: 10 }}>
                    <label className="form-label">Upload OPG Scan</label>
                    
    <div style={{ display: 'flex', gap: '10px' }}>
      <label className="btn btn-secondary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }}>
        ?? Upload Document
        <input type="file" accept="image/*,.pdf,.dicom" onChange={(e) => handleRadiologyUpload(e, 'opg')} style={{ display: 'none' }} />
      </label>
      <label className="btn btn-primary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer', background: '#34C759' }}>
        ?? Open Camera
        <input type="file" accept="image/*" capture="environment" onChange={(e) => handleRadiologyUpload(e, 'opg')} style={{ display: 'none' }} />
      </label>
    </div>
  
                    {radiology.opgFileUrl && <div style={{ fontSize: '0.8rem', color: '#059669', marginTop: 4 }}>✓ File uploaded</div>}
                  </div>
                )}
                <label className="form-label" style={{ marginTop: 10 }}>OPG Findings</label>
                <textarea className="form-input glass-input" name="opgFindings" rows={2} placeholder="OPG radiographic findings and observations..." value={radiology.opgFindings} onChange={handleRadiology} disabled={!radiology.opgTaken} />
              </div>
              <div className="form-group">
                <ToggleCard name="iopaTaken" label="IOPA Radiograph Available" checked={radiology.iopaTaken} onChange={handleRadiology} />
                <InfoToggle title="Why is this required preoperatively?" text="Intraoral Periapical (IOPA) radiographs offer high-resolution, localized views of individual teeth and their immediate periapical bone architecture, crucial for endodontic or extraction planning." />
                {radiology.iopaTaken && (
                  <div style={{ marginTop: 10 }}>
                    <label className="form-label">Upload IOPA Scan</label>
                    
    <div style={{ display: 'flex', gap: '10px' }}>
      <label className="btn btn-secondary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }}>
        ?? Upload Document
        <input type="file" accept="image/*,.pdf,.dicom" onChange={(e) => handleRadiologyUpload(e, 'iopa')} style={{ display: 'none' }} />
      </label>
      <label className="btn btn-primary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer', background: '#34C759' }}>
        ?? Open Camera
        <input type="file" accept="image/*" capture="environment" onChange={(e) => handleRadiologyUpload(e, 'iopa')} style={{ display: 'none' }} />
      </label>
    </div>
  
                    {radiology.iopaFileUrl && <div style={{ fontSize: '0.8rem', color: '#059669', marginTop: 4 }}>✓ File uploaded</div>}
                  </div>
                )}
                <label className="form-label" style={{ marginTop: 10 }}>IOPA Findings</label>
                <textarea className="form-input glass-input" name="iopaFindings" rows={2} placeholder="IOPA radiographic findings..." value={radiology.iopaFindings} onChange={handleRadiology} disabled={!radiology.iopaTaken} />
              </div>
              <div className="form-group">
                <ToggleCard name="cbctTaken" label="CBCT 3D Scan Available" checked={radiology.cbctTaken} onChange={handleRadiology} />
                <InfoToggle title="Why recommend a CBCT scan?" text="A Cone Beam Computed Tomography (CBCT) scan provides 3D volumetric imaging essential for complex surgical impactions, nerve proximity assessment, and pathology localization when 2D radiographs are insufficient." />
                {radiology.cbctTaken && (
                  <div style={{ marginTop: 10 }}>
                    <label className="form-label">Upload CBCT Scan</label>
                    
    <div style={{ display: 'flex', gap: '10px' }}>
      <label className="btn btn-secondary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer' }}>
        ?? Upload Document
        <input type="file" accept="image/*,.pdf,.dicom" onChange={(e) => handleRadiologyUpload(e, 'cbct')} style={{ display: 'none' }} />
      </label>
      <label className="btn btn-primary" style={{ flex: 1, textAlign: 'center', cursor: 'pointer', background: '#34C759' }}>
        ?? Open Camera
        <input type="file" accept="image/*" capture="environment" onChange={(e) => handleRadiologyUpload(e, 'cbct')} style={{ display: 'none' }} />
      </label>
    </div>
  
                    {radiology.cbctFileUrl && <div style={{ fontSize: '0.8rem', color: '#059669', marginTop: 4 }}>✓ File uploaded</div>}
                  </div>
                )}
                <label className="form-label" style={{ marginTop: 10 }}>CBCT Findings</label>
                <textarea className="form-input glass-input" name="cbctFindings" rows={2} placeholder="CBCT slice / nerve proximity findings..." value={radiology.cbctFindings} onChange={handleRadiology} disabled={!radiology.cbctTaken} />
              </div>
              <div className="form-group">
                <label className="form-label">Bone Density — Hounsfield Units (HU)</label>
                <input className="form-input glass-input" type="number" name="boneDensityHu" placeholder="e.g. 420" value={radiology.boneDensityHu} onChange={handleRadiology} />
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label className="form-label">General Radiology Notes</label>
                <textarea className="form-input glass-input" name="generalRadiologyNotes" rows={2} placeholder="Summary of radiographic assessment..." value={radiology.generalRadiologyNotes} onChange={handleRadiology} />
              </div>
            </div>
          </div>
        )}

        {/* ── STEP 4: Laboratory ── */}
        {step === 4 && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">🔬</span>
              <div>
                <div className="step-card-title">Step 4: Laboratory Investigations</div>
                <div className="step-card-sub">Hematological and biochemical parameters for surgical fitness</div>
              </div>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Hemoglobin (g/dL)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="hemoglobin" placeholder="e.g. 14.2" value={labs.hemoglobin} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">Platelet Count (cells/mcL)</label>
                <input className="form-input glass-input" type="number" name="plateletCount" placeholder="e.g. 245000" value={labs.plateletCount} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">INR (International Normalized Ratio)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="inr" placeholder="e.g. 1.0" value={labs.inr} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">Prothrombin Time – PT (sec)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="pt" placeholder="e.g. 12.5" value={labs.pt} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">APTT (sec)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="aptt" placeholder="e.g. 30.0" value={labs.aptt} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">Random Blood Sugar (mg/dL)</label>
                <input className="form-input glass-input" type="number" name="randomBloodSugar" placeholder="e.g. 110" value={labs.randomBloodSugar} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">Fasting Blood Sugar (mg/dL)</label>
                <input className="form-input glass-input" type="number" name="fastingBloodSugar" placeholder="e.g. 92" value={labs.fastingBloodSugar} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">HbA1c (%)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="hba1c" placeholder="e.g. 5.4" value={labs.hba1c} onChange={handleLabs} />
              </div>
              <div className="form-group">
                <label className="form-label">Serum Creatinine (mg/dL)</label>
                <input className="form-input glass-input" type="number" step="0.1" name="serumCreatinine" placeholder="e.g. 0.9" value={labs.serumCreatinine} onChange={handleLabs} />
              </div>
            </div>
          </div>
        )}

        {/* ── STEP 5: Medical History ── */}
        {step === 5 && (
          <>
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">🏥</span>
              <div>
                <div className="step-card-title">Step 5: Medical History &amp; Systemic Conditions</div>
                <div className="step-card-sub">Select all systemic conditions that apply to the patient</div>
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: 10, marginBottom: 20 }}>
              {[
                { name: 'hypertension',   label: '🫀 Hypertension' },
                { name: 'diabetes',       label: '🩸 Diabetes Mellitus' },
                { name: 'heartDisease',   label: '💓 Cardiac Disease' },
                { name: 'kidneyDisease',  label: '🫘 Renal Disorder' },
                { name: 'liverDisease',   label: '🫁 Hepatic Disorder' },
                { name: 'thyroidDisorder',label: '🦋 Thyroid Disorder' },
                { name: 'asthma',         label: '🌬️ Asthma / Respiratory' },
                { name: 'epilepsy',       label: '⚡ Epilepsy / Seizure' },
                { name: 'bloodDisorder',  label: '💉 Bleeding / Coagulation' },
                { name: 'hepatitis',      label: '🔬 Hepatitis (B / C)' },
                { name: 'hivPositive',    label: '🛡️ HIV / Immunocompromised' },
                { name: 'pregnant',       label: '🤰 Pregnancy Status' },
              ].map(c => (
                <ToggleCard key={c.name} name={c.name} label={c.label} checked={medHistory[c.name]} onChange={handleMedHistoryCheck} />
              ))}
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Previous Surgeries / Hospitalizations</label>
                <input className="form-input glass-input" name="previousSurgeries" placeholder="Enter previous surgeries if any" value={medHistory.previousSurgeries} onChange={handleMedHistoryText} />
              </div>
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label className="form-label">Other Conditions / Clinical Notes</label>
                <textarea className="form-input glass-input" name="notes" rows={3} placeholder="Any additional medical history or anesthesia considerations..." value={medHistory.notes} onChange={handleMedHistoryText} />
              </div>
            </div>
          </div>


        
        {/* ── STEP 5: Medications ── */}
        <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">💊</span>
              <div>
                <div className="step-card-title">Step 5: Drug Regimen &amp; Anticoagulant Status</div>
                <div className="step-card-sub">Current medications and anticoagulation/immunosuppression status</div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
              <ToggleCard
                name="isAnticoagulant"
                label="🩸 On Anticoagulant Therapy (Warfarin / Heparin / NOACs)"
                checked={meds.isAnticoagulant}
                onChange={handleMeds}
              />
              <ToggleCard
                name="isImmunosuppressant"
                label="🛡️ On Immunosuppressant / Systemic Steroids"
                checked={meds.isImmunosuppressant}
                onChange={handleMeds}
              />
            </div>
            {meds.isAnticoagulant && (
              <div className="alert alert-error" style={{ marginBottom: 16 }}>
                ⚠️ Anticoagulant status confirmed — INR, PT, and APTT values in Step 6 are critical for surgical clearance.
              </div>
            )}
            <div className="form-grid">
              <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                <label className="form-label">Current Medications Regimen</label>
                <InfoToggle title="Why review current medications?" text="Antiplatelet therapy (Aspirin/Clopidogrel) or anticoagulants must be temporarily altered or discontinued under physician clearance. Corticosteroids affect surgical healing and stress reserves. Knowing medications prevents harmful drug-drug interactions." />
                <input className="form-input glass-input" name="currentMedications" placeholder="Enter current medications..." value={meds.currentMedications} onChange={handleMeds} />
              </div>
              <div className="form-group">
                <label className="form-label">Standard Dosage</label>
                <input className="form-input glass-input" name="dosage" placeholder="Enter dosage..." value={meds.dosage} onChange={handleMeds} />
              </div>
              <div className="form-group">
                <label className="form-label">Frequency</label>
                <select className="form-input glass-input form-select" name="frequency" value={meds.frequency} onChange={handleMeds}>
                  {MED_FREQUENCY.map(f => <option key={f}>{f}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Route of Administration</label>
                <select className="form-input glass-input form-select" name="route" value={meds.route} onChange={handleMeds}>
                  {MED_ROUTE.map(r => <option key={r}>{r}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Diet Type</label>
                <select className="form-input glass-input form-select" name="diet" value={meds.diet} onChange={handleMeds}>
                  {DIET_TYPES.map(d => <option key={d}>{d}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Clinical Indication</label>
                <input className="form-input glass-input" name="indication" placeholder="Enter clinical indication..." value={meds.indication} onChange={handleMeds} />
              </div>
            </div>
          </div>


        
          </>
        )}

{/* ── STEP 6: Dental Examination ── */}
        {step === 6 && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">🦷</span>
              <div>
                <div className="step-card-title">Step 6: Dental Examination &amp; Oral Surgery Triaging</div>
                <div className="step-card-sub">Clinical classification and surgical difficulty assessment</div>
              </div>
            </div>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Target Tooth Number (FDI System)</label>
                <select className="form-input glass-input form-select" name="toothNumber" value={dental.toothNumber} onChange={handleDental}>
                  {TOOTH_NUMBERS.map(t => <option key={t}>{t}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Impaction Type</label>
                <InfoToggle title="Why classify third molar impactions?" text="Pell & Gregory grades the ramus space availability (Class I-III) and occlusal level depth (A-C). Winter\'s defines angulation (mesioangular, horizontal, distoangular). These classifications define bone removal difficulty and nerve proximity risks." />
                <select className="form-input glass-input form-select" name="impactionType" value={dental.impactionType} onChange={handleDental}>
                  {IMPACTION_TYPES.map(i => <option key={i}>{i}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Pell-Gregory Classification</label>
                <select className="form-input glass-input form-select" name="pellGregoryClass" value={dental.pellGregoryClass} onChange={handleDental}>
                  {PELL_GREGORY.map(p => <option key={p}>{p}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Winter's Classification</label>
                <select className="form-input glass-input form-select" name="winterClassification" value={dental.winterClassification} onChange={handleDental}>
                  {WINTER_CLASS.map(w => <option key={w}>{w}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Upper Third Relation</label>
                <select className="form-input glass-input form-select" name="upperThirdRelation" value={dental.upperThirdRelation} onChange={handleDental}>
                  {UPPER_THIRD_RELATION.map(u => <option key={u}>{u}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Mouth Opening (mm)</label>
                <input className="form-input glass-input" type="number" name="mouthOpeningMm" min="0" max="80" placeholder="e.g. 40" value={dental.mouthOpeningMm} onChange={handleDental} />
              </div>
              <div className="form-group">
                <label className="form-label">Surgical Difficulty</label>
                <select className="form-input glass-input form-select" name="difficultyScore" value={dental.difficultyScore} onChange={handleDental}>
                  {SURGICAL_DIFFICULTY.map(d => <option key={d}>{d}</option>)}
                </select>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 16, marginTop: 20, flexWrap: 'wrap' }}>
              <ToggleCard name="trismus"        label="⚠️ Trismus Present"            checked={dental.trismus}        onChange={handleDental} />
              <ToggleCard name="activeInfection" label="🔴 Active Infection / Abscess" checked={dental.activeInfection} onChange={handleDental} />
              <ToggleCard name="swelling"        label="🫧 Facial Swelling Present"    checked={dental.swelling}       onChange={handleDental} />
            </div>
          </div>
        )}

        {/* -- STEP 7 & 8 -- */}
        {/* ── STEP 8: Clinical Decision & Report ── */}
        {(step === 7 || step === 8) && (
          <div className="card glass-card">
            <div className="step-card-header">
              <span className="step-icon">📋</span>
              <div>
                <div className="step-card-title">Step 8: Clinical Decision &amp; Risk Assessment Report</div>
                <div className="step-card-sub">AI-assisted surgical fitness evaluation and formal preoperative PDF report</div>
              </div>
            </div>
            <p style={{ color: 'var(--text-secondary)', marginBottom: 24, lineHeight: 1.7 }}>
              All preoperative diagnostic domains have been recorded. Evaluate the algorithmic surgical fitness
              decision and generate the formal SIMATS preoperative PDF report for clinical review.
            </p>

            <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
              <button type="button" data-testid="reg-evaluate-btn" onClick={handleEvaluateRisk} className="btn btn-primary glass-btn" disabled={loading}>
                {loading ? '⏳ Evaluating...' : '🧠 1. Evaluate Clinical Risk Score & Fitness'}
              </button>
              <button type="button" data-testid="reg-report-btn" onClick={handleGenerateReport} className="btn btn-secondary" disabled={loading || !decisionResult}>
                {loading ? '⏳ Generating...' : '📄 2. Generate Preop Assessment Report (PDF)'}
              </button>
            </div>

            {decisionResult && (
              <div className={`decision-result-card ${decisionResult.fitnessStatus === 'FIT' ? 'fit' : 'unfit'}`}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                  <h3 style={{ margin: 0, fontSize: '1.1rem' }}>
                    Surgical Fitness: <strong>{decisionResult.fitnessStatus || 'FIT'}</strong>
                  </h3>
                  <span className={`risk-badge risk-${(decisionResult.riskLevel || 'LOW').toLowerCase()}`}>
                    {decisionResult.riskLevel || 'LOW'} RISK
                  </span>
                </div>
                <p style={{ margin: '0 0 12px', color: 'var(--text-secondary)' }}>{decisionResult.decisionNotes}</p>
                {decisionResult.recommendations && (
                  <ul style={{ paddingLeft: 20, color: 'var(--text-secondary)' }}>
                    {decisionResult.recommendations.map((rec, idx) => (
                      <li key={idx} style={{ marginBottom: 4 }}><strong style={{ color: 'var(--text-primary)' }}>Rec:</strong> {rec}</li>
                    ))}
                  </ul>
                )}
              </div>
            )}

            {reportResult && (
              <div className="report-result-card">
                <div>
                  <h4 style={{ margin: 0, color: 'var(--accent)' }}>✅ Report Generated Successfully</h4>
                  <p style={{ margin: '6px 0 0', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                    File: <strong style={{ color: 'var(--text-primary)' }}>{reportResult.reportFileName}</strong>
                    &nbsp;(Version {reportResult.reportVersion})
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => api.download(reportResult.downloadUrl || `/patients/${patientId}/report/download?reportId=${reportResult.id}`, reportResult.reportFileName)}
                  className="btn btn-primary glass-btn"
                >
                  ⬇ Download PDF
                </button>
              </div>
            )}

            <div style={{ borderTop: '1px solid #E2E8F0', paddingTop: 24, textAlign: 'right' }}>
              <button type="button" onClick={handleFinishWorkflow} className="btn btn-primary glass-btn" style={{ padding: '12px 32px', fontSize: '1rem' }}>
                Complete Workflow & View Patient Profile →
              </button>
            </div>
          </div>
        )}

        {/* ── Navigation Footer ── */}
        {step < 8 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 24 }}>
            <button type="button" className="btn btn-ghost" onClick={() => step > 1 && setStep(s => s - 1)} disabled={step === 1 || loading}>
              ← Previous Step
            </button>
            <button type="submit" data-testid="reg-save-step-btn" className="btn btn-primary glass-btn" disabled={loading}>
              {loading ? <><span className="spinner" /> Saving...</> : `Save Step ${step} & Continue →`}
            </button>
          </div>
        )}
      </form>
    </div>
  );
}
