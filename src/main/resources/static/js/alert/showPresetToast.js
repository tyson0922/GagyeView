
function showPresetToast(type, customTitle = '', customText = '', callback){
    const preset = {
        success: {
            icon: 'success',
            toast: true,
            position: 'center',
            showConfirmButton: true,
            confirmButtonText: '확인'
        },
        warning: {
            icon: 'warning',
            toast: true,
            position: 'center',
            showConfirmButton: true,
            confirmButtonText: '확인'
        },
        error: {
            icon:'error',
            toast:true,
            position: 'center',
            showConfirmButton: true,
            confirmButtonText: '확인'
        },
        commitWarning: {
            icon: 'warning',
            toast: true,
            position: 'center',
            showConfirmButton: true,
            confirmButtonText: '확인',
            showCancelButton: true,
            cancelButtonText: '취소'
        },
        SuccessBrief: {
            icon: 'success',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000
        }
    };

    const baseConfig = preset[type]

    if (baseConfig){
        Swal.fire({
            ...baseConfig,
            title: customTitle || baseConfig.title || '',
            text: customText || baseConfig.text || '',
            showClass: {
                // popup: 'animate_animated animate__bounce'
                popup: 'animate__animated animate__fadeIn'
            },
            hideClass: {
                popup: 'animate__animated animate__fadeOut'
            }
        }).then((result) => {
            if (result.isConfirmed && typeof callback === 'function') {
                callback(result);
            }
        })
    } else {
        console.error('Invalid toast type:', type)
    }
}